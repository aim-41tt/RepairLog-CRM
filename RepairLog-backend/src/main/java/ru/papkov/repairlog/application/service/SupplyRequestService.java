package ru.papkov.repairlog.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.supply.*;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.model.enums.SupplyRequestSource;
import ru.papkov.repairlog.domain.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис управления заявками на поставку.
 * Создание заявок техниками, авто-формирование, подтверждение администратором, приёмка на склад.
 *
 * @author aim-41tt
 */
@Service
@Transactional(readOnly = true)
public class SupplyRequestService {

    private static final Logger log = LoggerFactory.getLogger(SupplyRequestService.class);

    private final SupplyRequestRepository supplyRequestRepository;
    private final SupplyRequestItemRepository supplyRequestItemRepository;
    private final SupplyRequestStatusRepository statusRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final RepairOrderRepository repairOrderRepository;

    public SupplyRequestService(SupplyRequestRepository supplyRequestRepository,
                                SupplyRequestItemRepository supplyRequestItemRepository,
                                SupplyRequestStatusRepository statusRepository,
                                SupplierRepository supplierRepository,
                                EmployeeRepository employeeRepository,
                                InventoryItemRepository inventoryItemRepository,
                                RepairOrderRepository repairOrderRepository) {
        this.supplyRequestRepository = supplyRequestRepository;
        this.supplyRequestItemRepository = supplyRequestItemRepository;
        this.statusRepository = statusRepository;
        this.supplierRepository = supplierRepository;
        this.employeeRepository = employeeRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.repairOrderRepository = repairOrderRepository;
    }

    // ========== Чтение ==========

    public List<SupplyRequestResponse> getAll() {
        return supplyRequestRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<SupplyRequestResponse> getByStatus(String statusName) {
        SupplyRequestStatus status = statusRepository.findByName(statusName)
                .orElseThrow(() -> new EntityNotFoundException("Статус не найден: " + statusName));
        return supplyRequestRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SupplyRequestResponse getById(Long id) {
        return toResponse(findById(id));
    }

    public List<SupplyRequestResponse> getByEmployee(String login) {
        Employee employee = employeeRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));
        return supplyRequestRepository.findByRequestedBy(employee).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ========== Создание (ручное — техник/админ) ==========

    @Transactional
    public SupplyRequestResponse create(CreateSupplyRequestRequest request, String requestedByLogin) {
        Employee requestedBy = employeeRepository.findByLogin(requestedByLogin)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new EntityNotFoundException("Поставщик не найден"));
        }

        RepairOrder repairOrder = null;
        if (request.getRepairOrderId() != null) {
            repairOrder = repairOrderRepository.findById(request.getRepairOrderId())
                    .orElseThrow(() -> new EntityNotFoundException("Заказ на ремонт не найден"));
        }

        SupplyRequestStatus newStatus = getStatus(SupplyStatusConstants.NEW);

        String requestNumber = generateRequestNumber();

        SupplyRequest supplyRequest = new SupplyRequest();
        supplyRequest.setRequestNumber(requestNumber);
        supplyRequest.setSupplier(supplier);
        supplyRequest.setStatus(newStatus);
        supplyRequest.setRequestedBy(requestedBy);
        supplyRequest.setRelatedRepairOrder(repairOrder);
        supplyRequest.setComment(request.getComment());
        supplyRequest.setSource(SupplyRequestSource.MANUAL);

        SupplyRequest saved = supplyRequestRepository.save(supplyRequest);

        BigDecimal total = createItems(saved, request.getItems());
        saved.setTotalAmount(total);
        supplyRequestRepository.save(saved);

        log.info("Создана заявка на поставку {} сотрудником {}", requestNumber, requestedByLogin);
        return toResponse(saved);
    }

    // ========== Создание (авто-формирование) ==========

    @Transactional
    public SupplyRequest createAutoReorder(InventoryItem item, int quantity, Employee systemUser) {
        SupplyRequestStatus autoFormedStatus = getStatus(SupplyStatusConstants.AUTO_FORMED);

        String requestNumber = generateRequestNumber();

        SupplyRequest supplyRequest = new SupplyRequest();
        supplyRequest.setRequestNumber(requestNumber);
        supplyRequest.setSupplier(item.getPreferredSupplier());
        supplyRequest.setStatus(autoFormedStatus);
        supplyRequest.setRequestedBy(systemUser);
        supplyRequest.setSource(SupplyRequestSource.AUTO_REORDER);
        supplyRequest.setComment("Авто-заказ: остаток " + item.getQuantity()
                + " ниже минимума " + item.getMinStockLevel());

        SupplyRequest saved = supplyRequestRepository.save(supplyRequest);

        // создаём одну позицию
        SupplyRequestItem requestItem = new SupplyRequestItem();
        requestItem.setSupplyRequest(saved);
        requestItem.setInventoryItem(item);
        requestItem.setItemName(item.getName());
        requestItem.setQuantity(quantity);

        BigDecimal price = item.getCurrentMarketPrice() != null
                ? item.getCurrentMarketPrice()
                : (item.getLastPurchasePrice() != null ? item.getLastPurchasePrice() : BigDecimal.ZERO);
        requestItem.setUnitPrice(price);
        requestItem.setTotalPrice(price.multiply(BigDecimal.valueOf(quantity)));

        supplyRequestItemRepository.save(requestItem);

        saved.setTotalAmount(requestItem.getTotalPrice());
        supplyRequestRepository.save(saved);

        log.info("Авто-сформирована заявка {} для товара '{}', кол-во: {}",
                requestNumber, item.getName(), quantity);
        return saved;
    }

    // ========== Жизненный цикл заявки ==========

    @Transactional
    public SupplyRequestResponse approve(Long id, String approvedByLogin) {
        SupplyRequest request = findById(id);
        String currentStatus = request.getStatus().getName();
        if (!SupplyStatusConstants.NEW.equals(currentStatus)
                && !SupplyStatusConstants.AUTO_FORMED.equals(currentStatus)) {
            throw new BusinessLogicException(
                    "Нельзя подтвердить заявку из статуса '" + currentStatus + "'");
        }

        Employee approvedBy = employeeRepository.findByLogin(approvedByLogin)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));

        request.setStatus(getStatus(SupplyStatusConstants.APPROVED));
        request.setApprovedBy(approvedBy);

        log.info("Заявка {} подтверждена администратором {}", request.getRequestNumber(), approvedByLogin);
        return toResponse(supplyRequestRepository.save(request));
    }

    @Transactional
    public SupplyRequestResponse markOrdered(Long id) {
        SupplyRequest request = findById(id);
        validateStatusTransition(request, SupplyStatusConstants.APPROVED, SupplyStatusConstants.ORDERED);

        request.setStatus(getStatus(SupplyStatusConstants.ORDERED));

        log.info("Заявка {} отмечена как заказанная", request.getRequestNumber());
        return toResponse(supplyRequestRepository.save(request));
    }

    @Transactional
    public SupplyRequestResponse markInTransit(Long id) {
        SupplyRequest request = findById(id);
        validateStatusTransition(request, SupplyStatusConstants.ORDERED, SupplyStatusConstants.IN_TRANSIT);

        request.setStatus(getStatus(SupplyStatusConstants.IN_TRANSIT));

        log.info("Заявка {} в пути", request.getRequestNumber());
        return toResponse(supplyRequestRepository.save(request));
    }

    @Transactional
    public SupplyRequestResponse markDelivered(Long id) {
        SupplyRequest request = findById(id);
        String currentStatus = request.getStatus().getName();
        if (!SupplyStatusConstants.ORDERED.equals(currentStatus)
                && !SupplyStatusConstants.IN_TRANSIT.equals(currentStatus)) {
            throw new BusinessLogicException(
                    "Нельзя отметить доставленной заявку из статуса '" + currentStatus + "'");
        }

        request.setStatus(getStatus(SupplyStatusConstants.DELIVERED));

        // обновляем last_purchase_price у привязанных inventory_items
        if (request.getItems() != null) {
            for (SupplyRequestItem item : request.getItems()) {
                if (item.getInventoryItem() != null && item.getUnitPrice() != null) {
                    InventoryItem inv = item.getInventoryItem();
                    inv.setLastPurchasePrice(item.getUnitPrice());
                    inventoryItemRepository.save(inv);
                }
            }
        }

        log.info("Заявка {} отмечена как доставленная", request.getRequestNumber());
        return toResponse(supplyRequestRepository.save(request));
    }

    @Transactional
    public SupplyRequestResponse cancel(Long id) {
        SupplyRequest request = findById(id);
        if (SupplyStatusConstants.DELIVERED.equals(request.getStatus().getName())) {
            throw new BusinessLogicException("Нельзя отменить доставленную заявку");
        }

        request.setStatus(getStatus(SupplyStatusConstants.CANCELLED));
        log.info("Заявка {} отменена", request.getRequestNumber());
        return toResponse(supplyRequestRepository.save(request));
    }

    @Transactional
    public SupplyRequestResponse assignSupplier(Long id, Long supplierId) {
        SupplyRequest request = findById(id);
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Поставщик не найден"));

        request.setSupplier(supplier);
        log.info("К заявке {} привязан поставщик {}", request.getRequestNumber(), supplier.getName());
        return toResponse(supplyRequestRepository.save(request));
    }

    @Transactional
    public void updateExternalOrderInfo(String externalOrderId, String externalStatus) {
        // поиск заявки по externalOrderId через все заявки (можно оптимизировать)
        List<SupplyRequest> all = supplyRequestRepository.findAll();
        for (SupplyRequest sr : all) {
            if (externalOrderId.equals(sr.getExternalOrderId())) {
                sr.setExternalOrderStatus(externalStatus);
                supplyRequestRepository.save(sr);
                log.info("Обновлен статус внешнего заказа {} -> {}", externalOrderId, externalStatus);
                return;
            }
        }
        log.warn("Заявка с externalOrderId={} не найдена", externalOrderId);
    }

    // ========== Вспомогательные методы ==========

    private SupplyRequest findById(Long id) {
        return supplyRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заявка на поставку не найдена: " + id));
    }

    private SupplyRequestStatus getStatus(String name) {
        return statusRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Статус не найден: " + name));
    }

    private String generateRequestNumber() {
        return "SR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%04d", supplyRequestRepository.count() + 1);
    }

    private void validateStatusTransition(SupplyRequest request, String expectedCurrent, String target) {
        if (!expectedCurrent.equals(request.getStatus().getName())) {
            throw new BusinessLogicException(
                    "Нельзя перевести заявку из статуса '" + request.getStatus().getName()
                            + "' в '" + target + "'");
        }
    }

    private BigDecimal createItems(SupplyRequest saved, List<CreateSupplyRequestItemRequest> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (CreateSupplyRequestItemRequest itemReq : items) {
            SupplyRequestItem item = new SupplyRequestItem();
            item.setSupplyRequest(saved);
            item.setItemName(itemReq.getItemName());
            item.setPartNumber(itemReq.getPartNumber());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : BigDecimal.ZERO);

            // привязка к складской позиции (если указана)
            if (itemReq.getInventoryItemId() != null) {
                InventoryItem inv = inventoryItemRepository.findById(itemReq.getInventoryItemId())
                        .orElseThrow(() -> new EntityNotFoundException("Товар не найден"));
                item.setInventoryItem(inv);
            }

            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setTotalPrice(lineTotal);
            total = total.add(lineTotal);

            supplyRequestItemRepository.save(item);
        }
        return total;
    }

    private SupplyRequestResponse toResponse(SupplyRequest sr) {
        SupplyRequestResponse r = new SupplyRequestResponse();
        r.setId(sr.getId());
        r.setRequestNumber(sr.getRequestNumber());
        r.setSupplierName(sr.getSupplier() != null ? sr.getSupplier().getName() : null);
        r.setSupplierId(sr.getSupplier() != null ? sr.getSupplier().getId() : null);
        r.setStatusName(sr.getStatus() != null ? sr.getStatus().getName() : null);
        r.setRequestedByName(sr.getRequestedBy() != null ? sr.getRequestedBy().getFullName() : null);
        r.setApprovedByName(sr.getApprovedBy() != null ? sr.getApprovedBy().getFullName() : null);
        r.setTotalAmount(sr.getTotalAmount());
        r.setComment(sr.getComment());
        r.setCreatedAt(sr.getCreatedAt());
        r.setExpectedDeliveryDate(sr.getExpectedDeliveryDate());
        r.setSource(sr.getSource() != null ? sr.getSource().name() : null);
        r.setExternalOrderId(sr.getExternalOrderId());
        r.setExternalOrderStatus(sr.getExternalOrderStatus());

        if (sr.getRelatedRepairOrder() != null) {
            r.setRelatedRepairOrderId(sr.getRelatedRepairOrder().getId());
            r.setRelatedOrderNumber(sr.getRelatedRepairOrder().getOrderNumber());
        }

        if (sr.getItems() != null) {
            r.setItems(sr.getItems().stream().map(item -> {
                SupplyRequestItemResponse ir = new SupplyRequestItemResponse();
                ir.setId(item.getId());
                ir.setItemName(item.getItemName());
                ir.setPartNumber(item.getPartNumber());
                ir.setQuantity(item.getQuantity());
                ir.setUnitPrice(item.getUnitPrice());
                ir.setTotalPrice(item.getTotalPrice());
                return ir;
            }).collect(Collectors.toList()));
        }

        return r;
    }
}
