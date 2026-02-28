package ru.papkov.repairlog.infrastructure.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.service.SupplyRequestService;
import ru.papkov.repairlog.application.service.SupplySettingService;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.InventoryItem;
import ru.papkov.repairlog.domain.repository.EmployeeRepository;
import ru.papkov.repairlog.domain.repository.InventoryItemRepository;

import java.util.List;

/**
 * Планировщик авто-заказа товаров при низком остатке.
 * Формирует заявки AUTO_FORMED для товаров ниже минимального уровня.
 */
@Component
public class AutoReorderScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutoReorderScheduler.class);

    private final InventoryItemRepository inventoryItemRepository;
    private final SupplyRequestService supplyRequestService;
    private final SupplySettingService settingService;
    private final EmployeeRepository employeeRepository;

    public AutoReorderScheduler(InventoryItemRepository inventoryItemRepository,
                                 SupplyRequestService supplyRequestService,
                                 SupplySettingService settingService,
                                 EmployeeRepository employeeRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.supplyRequestService = supplyRequestService;
        this.settingService = settingService;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public void checkAndReorder() {
        if (!settingService.getBooleanValue("auto.reorder.enabled", false)) {
            log.debug("Авто-заказ отключён в настройках");
            return;
        }

        // системный пользователь для авто-заявок
        String systemLogin = settingService.getValue("system.user.login", "admin");
        Employee systemUser = employeeRepository.findByLogin(systemLogin).orElse(null);
        if (systemUser == null) {
            log.error("Системный пользователь '{}' не найден, авто-заказ невозможен", systemLogin);
            return;
        }

        List<InventoryItem> itemsToReorder = inventoryItemRepository.findItemsNeedingReorder();
        log.info("Авто-заказ: найдено {} позиций ниже минимального уровня", itemsToReorder.size());

        int created = 0;
        for (InventoryItem item : itemsToReorder) {
            if (item.getPreferredSupplier() == null) {
                log.debug("Пропущен товар '{}' — нет предпочтительного поставщика", item.getName());
                continue;
            }

            // рассчитываем количество для заказа
            int deficit = item.getMinStockLevel() - item.getQuantity();
            int reorderQty = item.getReorderQuantity() != null && item.getReorderQuantity() > 0
                    ? item.getReorderQuantity()
                    : deficit;

            // округление по pack_size: ceil(qty / packSize) * packSize
            int packSize = item.getPackSize() != null && item.getPackSize() > 1 ? item.getPackSize() : 1;
            int orderQty = (int) Math.ceil((double) reorderQty / packSize) * packSize;

            if (orderQty <= 0) {
                continue;
            }

            try {
                supplyRequestService.createAutoReorder(item, orderQty, systemUser);
                created++;
            } catch (Exception e) {
                log.error("Ошибка авто-заказа для товара '{}': {}", item.getName(), e.getMessage());
            }
        }

        log.info("Авто-заказ завершён: создано {} заявок", created);
    }
}
