package ru.papkov.repairlog.application.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.order.ChangeStatusRequest;
import ru.papkov.repairlog.application.dto.order.CreateRepairOrderRequest;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.repository.*;

import java.util.List;
import java.util.Optional;

/**
 * Сервис управления заказами на ремонт.
 * Основной бизнес-процесс системы: приёмка → диагностика → ремонт → выдача.
 * <p>
 * Возвращает entity — DTO-конверсия выполняется в контроллерах через маппер.
 * Поля totalAmount/paymentStatus в DTO заполняются контроллером через
 * {@link #findReceiptByOrder(RepairOrder)}.
 * </p>
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
    private final EntityManager entityManager;

    public RepairOrderService(RepairOrderRepository repairOrderRepository,
                              ClientRepository clientRepository,
                              DeviceRepository deviceRepository,
                              EmployeeRepository employeeRepository,
                              RepairStatusRepository repairStatusRepository,
                              RepairPriorityRepository repairPriorityRepository,
                              StatusHistoryRepository statusHistoryRepository,
                              ReceiptRepository receiptRepository,
                              EntityManager entityManager) {
        this.repairOrderRepository = repairOrderRepository;
        this.clientRepository = clientRepository;
        this.deviceRepository = deviceRepository;
        this.employeeRepository = employeeRepository;
        this.repairStatusRepository = repairStatusRepository;
        this.repairPriorityRepository = repairPriorityRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.receiptRepository = receiptRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<RepairOrder> getAllActive() {
        return repairOrderRepository.findAllActiveOrders();
    }

    @Transactional(readOnly = true)
    public List<RepairOrder> getUnassigned() {
        return repairOrderRepository.findUnassignedOrders();
    }

    @Transactional(readOnly = true)
    public List<RepairOrder> getByMaster(Long masterId) {
        Employee master = employeeRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Мастер не найден: " + masterId));
        return repairOrderRepository.findActiveOrdersByMaster(master);
    }

    @Transactional(readOnly = true)
    public RepairOrder getById(Long id) {
        return findOrder(id);
    }

    @Transactional(readOnly = true)
    public RepairOrder getByOrderNumber(String orderNumber) {
        return repairOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден: " + orderNumber));
    }

    @Transactional(readOnly = true)
    public List<RepairOrder> getByClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден: " + clientId));
        return repairOrderRepository.findByClient(client);
    }

    /**
     * Создание заказа на ремонт (RECEPTIONIST).
     * Статус автоматически устанавливается "Новая".
     */
    @Transactional
    public RepairOrder create(CreateRepairOrderRequest request, String acceptedByLogin) {
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

        RepairOrder saved = repairOrderRepository.saveAndFlush(order);

        // перечитываем entity, чтобы получить orderNumber, сгенерированный триггером БД
        entityManager.refresh(saved);

        // записываем в историю статусов
        recordStatusChange(saved, newStatus, acceptedBy, "Заказ создан");

        // возвращаем с загруженными lazy-ассоциациями (device.deviceType, model.brand и др.)
        return findOrder(saved.getId());
    }

    /**
     * Назначение мастера на заказ (TECHNICIAN берёт заказ или ADMIN назначает).
     */
    @Transactional
    public RepairOrder assignMaster(Long orderId, Long masterId) {
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

        return saved;
    }

    /**
     * Изменение статуса заказа.
     */
    @Transactional
    public RepairOrder changeStatus(Long orderId, ChangeStatusRequest request, String changedByLogin) {
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

        return saved;
    }

    @Transactional(readOnly = true)
    public List<StatusHistory> getStatusHistory(Long orderId) {
        RepairOrder order = findOrder(orderId);
        return statusHistoryRepository.findByRepairOrderOrderByChangedAtDesc(order);
    }

    @Transactional(readOnly = true)
    public List<RepairOrder> searchMultiField(String query) {
        return repairOrderRepository.searchMultiField(query);
    }

    /**
     * Возвращает чек, привязанный к заказу (если существует).
     * Используется контроллером для дополнения DTO полями totalAmount/paymentStatus.
     */
    @Transactional(readOnly = true)
    public Optional<Receipt> findReceiptByOrder(RepairOrder order) {
        return receiptRepository.findByRepairOrder(order);
    }

    // ========== Helpers ==========

    private RepairOrder findOrder(Long id) {
        return repairOrderRepository.findByIdWithDetails(id)
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
}
