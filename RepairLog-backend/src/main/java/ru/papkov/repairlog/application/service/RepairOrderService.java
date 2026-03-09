package ru.papkov.repairlog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.order.*;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.repository.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис управления заказами на ремонт.
 * Основной бизнес-процесс системы: приёмка → диагностика → ремонт → выдача.
 *
 * @author aim-41tt
 */
@Service
public class RepairOrderService {

    private final RepairOrderRepository repairOrderRepository;
    private final ClientRepository clientRepository;
    private final DeviceRepository deviceRepository;
    private final EmployeeRepository employeeRepository;
    private final RepairStatusRepository repairStatusRepository;
    private final RepairPriorityRepository repairPriorityRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final ReceiptRepository receiptRepository;

    public RepairOrderService(RepairOrderRepository repairOrderRepository,
                              ClientRepository clientRepository,
                              DeviceRepository deviceRepository,
                              EmployeeRepository employeeRepository,
                              RepairStatusRepository repairStatusRepository,
                              RepairPriorityRepository repairPriorityRepository,
                              StatusHistoryRepository statusHistoryRepository,
                              ReceiptRepository receiptRepository) {
        this.repairOrderRepository = repairOrderRepository;
        this.clientRepository = clientRepository;
        this.deviceRepository = deviceRepository;
        this.employeeRepository = employeeRepository;
        this.repairStatusRepository = repairStatusRepository;
        this.repairPriorityRepository = repairPriorityRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.receiptRepository = receiptRepository;
    }

    @Transactional(readOnly = true)
    public List<RepairOrderResponse> getAllActive() {
        return repairOrderRepository.findAllActiveOrders().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RepairOrderResponse> getUnassigned() {
        return repairOrderRepository.findUnassignedOrders().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RepairOrderResponse> getByMaster(Long masterId) {
        Employee master = employeeRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Мастер не найден: " + masterId));
        return repairOrderRepository.findActiveOrdersByMaster(master).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RepairOrderResponse getById(Long id) {
        return toResponse(findOrder(id));
    }

    @Transactional(readOnly = true)
    public RepairOrderResponse getByOrderNumber(String orderNumber) {
        return toResponse(repairOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден: " + orderNumber)));
    }

    @Transactional(readOnly = true)
    public List<RepairOrderResponse> getByClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден: " + clientId));
        return repairOrderRepository.findByClient(client).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Создание заказа на ремонт (RECEPTIONIST).
     * Статус автоматически устанавливается "Новая".
     */
    @Transactional
    public RepairOrderResponse create(CreateRepairOrderRequest request, String acceptedByLogin) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден"));
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new EntityNotFoundException("Устройство не найдено"));

        // Проверяем принадлежность устройства клиенту (если устройство клиентское)
        if (device.getClient() != null && !device.getClient().getId().equals(client.getId())) {
            throw new BusinessLogicException(
                    "Устройство #" + device.getId() + " не принадлежит клиенту #" + client.getId());
        }

        Employee acceptedBy = employeeRepository.findByLogin(acceptedByLogin)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));
        RepairStatus newStatus = repairStatusRepository.findByCode("NEW")
                .orElseThrow(() -> new EntityNotFoundException("Статус 'NEW' не найден"));

        RepairOrder order = new RepairOrder();
        order.setClient(client);
        order.setDevice(device);
        order.setAcceptedBy(acceptedBy);
        order.setCurrentStatus(newStatus);
        order.setClientComplaint(request.getClientComplaint());
        order.setExternalCondition(request.getExternalCondition());
        order.setWarrantyRepair(request.isWarrantyRepair());
        order.setEstimatedCompletionDate(request.getEstimatedCompletionDate());

        if (request.getPriorityId() != null) {
            RepairPriority priority = repairPriorityRepository.findById(request.getPriorityId())
                    .orElseThrow(() -> new EntityNotFoundException("Приоритет не найден"));
            order.setPriority(priority);
        }

        RepairOrder saved = repairOrderRepository.save(order);

        // записываем в историю статусов
        recordStatusChange(saved, newStatus, acceptedBy, "Заказ создан");

        return toResponse(saved);
    }

    /**
     * Назначение мастера на заказ (TECHNICIAN берёт заказ или ADMIN назначает).
     */
    @Transactional
    public RepairOrderResponse assignMaster(Long orderId, Long masterId) {
        RepairOrder order = findOrder(orderId);
        Employee master = employeeRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Мастер не найден"));

        if (!master.hasRole("TECHNICIAN")) {
            throw new BusinessLogicException("Сотрудник не является техником");
        }

        order.assignMaster(master);

        // переводим в статус "Принята"
        RepairStatus accepted = repairStatusRepository.findByCode("ACCEPTED")
                .orElseThrow(() -> new EntityNotFoundException("Статус 'ACCEPTED' не найден"));
        order.setCurrentStatus(accepted);

        RepairOrder saved = repairOrderRepository.save(order);
        recordStatusChange(saved, accepted, master, "Мастер назначен: " + master.getFullName());

        return toResponse(saved);
    }

    /**
     * Изменение статуса заказа.
     */
    @Transactional
    public RepairOrderResponse changeStatus(Long orderId, ChangeStatusRequest request, String changedByLogin) {
        RepairOrder order = findOrder(orderId);
        RepairStatus newStatus = repairStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new EntityNotFoundException("Статус не найден"));
        Employee changedBy = employeeRepository.findByLogin(changedByLogin)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));

        order.setCurrentStatus(newStatus);

        // автоматически проставляем дату завершения для финальных статусов
        if (Boolean.TRUE.equals(newStatus.getIsFinal())) {
            order.complete();
        }

        RepairOrder saved = repairOrderRepository.save(order);
        recordStatusChange(saved, newStatus, changedBy, request.getComment());

        return toResponse(saved);
    }

    // ========== Helpers ==========

    private RepairOrder findOrder(Long id) {
        return repairOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден: " + id));
    }

    private void recordStatusChange(RepairOrder order, RepairStatus status, Employee changedBy, String comment) {
        StatusHistory history = new StatusHistory();
        history.setRepairOrder(order);
        history.setStatus(status);
        history.setChangedBy(changedBy);
        history.setComment(comment);
        statusHistoryRepository.save(history);
    }

    private RepairOrderResponse toResponse(RepairOrder o) {
        RepairOrderResponse r = new RepairOrderResponse();
        r.setId(o.getId());
        r.setOrderNumber(o.getOrderNumber());
        r.setClientId(o.getClient().getId());
        r.setClientFullName(o.getClient().getFullName());
        r.setClientPhone(o.getClient().getPhone());
        r.setDeviceId(o.getDevice().getId());
        r.setDeviceDescription(o.getDevice().getDescription());
        r.setAcceptedByName(o.getAcceptedBy().getFullName());
        r.setCurrentStatusName(o.getCurrentStatus().getName());
        r.setCurrentStatusId(o.getCurrentStatus().getId());

        if (o.getAssignedMaster() != null) {
            r.setAssignedMasterName(o.getAssignedMaster().getFullName());
            r.setAssignedMasterId(o.getAssignedMaster().getId());
        }
        if (o.getPriority() != null) {
            r.setPriorityName(o.getPriority().getName());
        }

        r.setClientComplaint(o.getClientComplaint());
        r.setExternalCondition(o.getExternalCondition());
        r.setWarrantyRepair(o.getWarrantyRepair());
        r.setEstimatedCompletionDate(o.getEstimatedCompletionDate());
        r.setActualCompletionDate(o.getActualCompletionDate());
        r.setCreatedAt(o.getCreatedAt());

        // подтягиваем данные чека, если чек уже создан
        receiptRepository.findByRepairOrder(o).ifPresentOrElse(
            receipt -> {
                r.setTotalAmount(receipt.getTotalAmount());
                r.setPaymentStatus(receipt.getPaymentStatus().name());
            },
            () -> {
                r.setTotalAmount(java.math.BigDecimal.ZERO);
                r.setPaymentStatus("UNPAID");
            }
        );

        return r;
    }
}
