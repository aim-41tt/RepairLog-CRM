package ru.papkov.repairlog.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.inventory.InventoryItemResponse;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.papkov.repairlog.application.dto.monitoring.PriceUpdateWebhookRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис управления складом.
 * Поддерживает приход, расход, резерв, списание товаров.
 *
 * @author aim-41tt
 */
@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final EmployeeRepository employeeRepository;

    public InventoryService(InventoryItemRepository inventoryItemRepository,
                            InventoryMovementRepository inventoryMovementRepository,
                            RepairOrderRepository repairOrderRepository,
                            EmployeeRepository employeeRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.repairOrderRepository = repairOrderRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getAll() {
        return inventoryItemRepository.findByInStockTrue().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<InventoryItemResponse> getAll(Pageable pageable) {
        return inventoryItemRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getById(Long id) {
        return toResponse(findItem(id));
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> search(String query) {
        return inventoryItemRepository.findByNameContainingIgnoreCase(query).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getLowStock() {
        return inventoryItemRepository.findLowStockItems().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Списание товара для ремонта.
     */
    @Transactional
    public void consumeForRepair(Long itemId, int quantity, Long repairOrderId, String employeeLogin) {
        InventoryItem item = findItem(itemId);
        RepairOrder order = repairOrderRepository.findById(repairOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        Employee employee = employeeRepository.findByLogin(employeeLogin)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));

        item.decreaseQuantity(quantity);
        inventoryItemRepository.save(item);

        InventoryMovement movement = new InventoryMovement(
                item, InventoryMovement.MovementType.РАСХОД, quantity,
                order, null, employee, "Списание для ремонта заказа " + order.getOrderNumber());
        inventoryMovementRepository.save(movement);
    }

    /**
     * Приход товара на склад (ADMIN).
     */
    @Transactional
    public void receiveStock(Long itemId, int quantity, String employeeLogin, String comment) {
        InventoryItem item = findItem(itemId);
        Employee employee = employeeRepository.findByLogin(employeeLogin)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));

        item.increaseQuantity(quantity);
        inventoryItemRepository.save(item);

        InventoryMovement movement = new InventoryMovement(
                item, InventoryMovement.MovementType.ПРИХОД, quantity,
                null, null, employee, comment);
        inventoryMovementRepository.save(movement);
    }

    /**
     * Обновление рыночных цен по данным из вебхука Сервиса Мониторинга.
     * Выполняется в единой транзакции — при ошибке откатываются все изменения.
     *
     * @param items список позиций с новыми ценами
     * @return количество обновлённых записей
     */
    @Transactional
    public int updatePricesFromWebhook(List<PriceUpdateWebhookRequest.PriceItem> items) {
        int updated = 0;
        for (PriceUpdateWebhookRequest.PriceItem priceItem : items) {
            var inventoryItems = inventoryItemRepository.findByNameContainingIgnoreCase(priceItem.getItemName());
            for (InventoryItem item : inventoryItems) {
                item.setCurrentMarketPrice(priceItem.getPrice());
                item.setPriceUpdatedAt(LocalDateTime.now());
                inventoryItemRepository.save(item);
                updated++;
            }
        }
        log.info("Обновлено {} позиций по ценам из вебхука", updated);
        return updated;
    }

    // ========== Helpers ==========

    private InventoryItem findItem(Long id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Товар не найден: " + id));
    }

    /**
     * Получить все позиции, нуждающиеся в дозаказе (для авто-заказа).
     */
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getItemsNeedingReorder() {
        return inventoryItemRepository.findItemsNeedingReorder().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    private InventoryItemResponse toResponse(InventoryItem item) {
        InventoryItemResponse r = new InventoryItemResponse();
        r.setId(item.getId());
        r.setName(item.getName());
        r.setSerialNumber(item.getSerialNumber());
        r.setDegreeWearName(item.getDegreeWear().getName());
        r.setDevice(item.getIsDevice());
        r.setUnitPrice(item.getUnitPrice());
        r.setQuantity(item.getQuantity());
        r.setInStock(item.getInStock());
        r.setMinStockLevel(item.getMinStockLevel());
        r.setCreatedAt(item.getCreatedAt());

        // новые поля supply management
        if (item.getPreferredSupplier() != null) {
            r.setPreferredSupplierId(item.getPreferredSupplier().getId());
            r.setPreferredSupplierName(item.getPreferredSupplier().getName());
        }
        r.setLastPurchasePrice(item.getLastPurchasePrice());
        r.setCurrentMarketPrice(item.getCurrentMarketPrice());
        r.setPriceUpdatedAt(item.getPriceUpdatedAt());
        r.setReorderQuantity(item.getReorderQuantity());
        r.setPackSize(item.getPackSize());

        // определяем статус запаса
        if (item.getQuantity() == 0) r.setStockStatus("OUT_OF_STOCK");
        else if (item.isBelowMinStock()) r.setStockStatus("LOW_STOCK");
        else r.setStockStatus("GOOD_STOCK");

        return r;
    }
}
