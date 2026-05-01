package ru.papkov.repairlog.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.inventory.CreateInventoryItemRequest;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.papkov.repairlog.application.dto.monitoring.PriceUpdateWebhookRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис управления складом.
 * Поддерживает приход, расход, резерв, списание товаров.
 * <p>
 * Возвращает entity — DTO-конверсия выполняется в контроллерах через маппер.
 * </p>
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
    private final DegreeWearRepository degreeWearRepository;

    public InventoryService(InventoryItemRepository inventoryItemRepository,
                            InventoryMovementRepository inventoryMovementRepository,
                            RepairOrderRepository repairOrderRepository,
                            EmployeeRepository employeeRepository,
                            DegreeWearRepository degreeWearRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.repairOrderRepository = repairOrderRepository;
        this.employeeRepository = employeeRepository;
        this.degreeWearRepository = degreeWearRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getAll() {
        return inventoryItemRepository.findByInStockTrue();
    }

    @Transactional(readOnly = true)
    public Page<InventoryItem> getAll(Pageable pageable) {
        // Используем findByInStockTrue для единообразия с getAll() (не-pageable версией)
        return inventoryItemRepository.findByInStockTrue(pageable);
    }

    @Transactional(readOnly = true)
    public InventoryItem getById(Long id) {
        return findItem(id);
    }

    /**
     * Создание новой складской позиции.
     * Используется для ручной постановки запчастей/устройств на учёт:
     * подарок, возврат от клиента, излишки, найденное на складе и т.д.
     */
    @Transactional
    public InventoryItem createItem(CreateInventoryItemRequest request, String adminLogin) {
        Employee employee = employeeRepository.findByLogin(adminLogin)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден: " + adminLogin));

        DegreeWear degreeWear = degreeWearRepository.findByName("Новое")
                .orElseThrow(() -> new EntityNotFoundException("Степень износа 'Новое' не найдена в справочнике"));

        InventoryItem item = new InventoryItem();
        item.setName(request.getName());
        // partNumber используется как serialNumber только для штучной позиции (quantity == 1):
        // DB CHECK chk_serial_quantity запрещает serialNumber + quantity != 1.
        // Если quantity > 1, partNumber игнорируется (не сохраняется) — фронт должен предупреждать пользователя.
        if (request.getPartNumber() != null && !request.getPartNumber().isBlank()
                && request.getQuantity() != null && request.getQuantity() == 1) {
            item.setSerialNumber(request.getPartNumber());
        }
        item.setDegreeWear(degreeWear);
        item.setIsDevice(false);
        item.setUnitPrice(request.getSellingPrice() != null ? request.getSellingPrice() : java.math.BigDecimal.ZERO);
        item.setQuantity(request.getQuantity());
        item.setInStock(request.getQuantity() > 0);
        item.setMinStockLevel(request.getMinQuantity() != null ? request.getMinQuantity() : 0);
        item.setLastPurchasePrice(request.getPurchasePrice());

        InventoryItem saved = inventoryItemRepository.save(item);

        if (request.getQuantity() > 0) {
            InventoryMovement movement = new InventoryMovement(
                    saved, InventoryMovement.MovementType.ПРИХОД, request.getQuantity(),
                    null, null, employee,
                    "Первичная постановка на учёт" +
                            (request.getDescription() != null ? ": " + request.getDescription() : ""));
            inventoryMovementRepository.save(movement);
        }

        log.info("Создана складская позиция '{}' (qty={}) администратором {}",
                saved.getName(), saved.getQuantity(), adminLogin);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> search(String query) {
        return inventoryItemRepository.findByNameContainingIgnoreCase(query);
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getLowStock() {
        return inventoryItemRepository.findLowStockItems();
    }

    /**
     * Списание товара для ремонта.
     * Для серийных позиций (serialNumber != null) всегда списывается 1 штука целиком
     * и позиция помечается как не в наличии (inStock = false), quantity остаётся = 1,
     * чтобы не нарушать DB CHECK-ограничение chk_serial_quantity.
     */
    @Transactional
    public void consumeForRepair(Long itemId, int quantity, Long repairOrderId, String employeeLogin) {
        InventoryItem item = findItem(itemId);
        RepairOrder order = repairOrderRepository.findById(repairOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        Employee employee = employeeRepository.findByLogin(employeeLogin)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));

        int actualQty;
        if (item.getSerialNumber() != null) {
            // Серийный номер — уникальное устройство, всегда списываем целиком
            actualQty = 1;
            item.setInStock(false);
            // quantity намеренно оставляем = 1 (DB: chk_serial_quantity запрещает quantity = 0)
        } else {
            actualQty = quantity;
            item.decreaseQuantity(actualQty);
        }
        inventoryItemRepository.save(item);

        InventoryMovement movement = new InventoryMovement(
                item, InventoryMovement.MovementType.РАСХОД, actualQty,
                order, null, employee, "Списание для ремонта заказа " + order.getOrderNumber());
        inventoryMovementRepository.save(movement);
    }

    /**
     * Удалить складскую позицию (ADMIN).
     */
    @Transactional
    public void deleteItem(Long itemId) {
        InventoryItem item = findItem(itemId);
        inventoryItemRepository.delete(item);
        log.info("Складская позиция id={} '{}' удалена", itemId, item.getName());
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
            // Используем точный поиск по имени вместо substring, чтобы не обновлять несвязанные позиции
            var inventoryItems = inventoryItemRepository.findByNameIgnoreCase(priceItem.getItemName());
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
        return inventoryItemRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Товар не найден: " + id));
    }

    /**
     * Получить все позиции, нуждающиеся в дозаказе (для авто-заказа).
     */
    @Transactional(readOnly = true)
    public List<InventoryItem> getItemsNeedingReorder() {
        return inventoryItemRepository.findItemsNeedingReorder();
    }
}
