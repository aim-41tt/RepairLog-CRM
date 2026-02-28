package ru.papkov.repairlog.infrastructure.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.monitoring.MonitoringPriceRequest;
import ru.papkov.repairlog.application.dto.monitoring.MonitoringPriceResponse;
import ru.papkov.repairlog.application.service.SupplySettingService;
import ru.papkov.repairlog.domain.model.InventoryItem;
import ru.papkov.repairlog.domain.model.Supplier;
import ru.papkov.repairlog.domain.model.enums.IntegrationType;
import ru.papkov.repairlog.domain.repository.InventoryItemRepository;
import ru.papkov.repairlog.domain.repository.SupplierRepository;
import ru.papkov.repairlog.infrastructure.integration.MonitoringServiceClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Планировщик обновления цен от Сервиса Мониторинга.
 */
@Component
public class PriceUpdateScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceUpdateScheduler.class);

    private final SupplierRepository supplierRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final MonitoringServiceClient monitoringClient;
    private final SupplySettingService settingService;

    public PriceUpdateScheduler(SupplierRepository supplierRepository,
                                 InventoryItemRepository inventoryItemRepository,
                                 MonitoringServiceClient monitoringClient,
                                 SupplySettingService settingService) {
        this.supplierRepository = supplierRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.monitoringClient = monitoringClient;
        this.settingService = settingService;
    }

    @Transactional
    public void updatePrices() {
        if (!settingService.getBooleanValue("price.update.enabled", false)) {
            log.debug("Обновление цен отключено в настройках");
            return;
        }

        if (!monitoringClient.isAvailable()) {
            log.warn("Сервис Мониторинга недоступен, пропускаем обновление цен");
            return;
        }

        // Берём поставщиков с API или PRICE_ONLY интеграцией
        List<Supplier> apiSuppliers = supplierRepository.findByIntegrationType(IntegrationType.FULL_AUTO);
        apiSuppliers.addAll(supplierRepository.findByIntegrationType(IntegrationType.PRICE_ONLY));

        log.info("Запуск обновления цен для {} поставщиков", apiSuppliers.size());

        for (Supplier supplier : apiSuppliers) {
            if (supplier.getExternalSupplierId() == null) {
                continue;
            }

            List<InventoryItem> items = inventoryItemRepository.findByPreferredSupplier(supplier);
            if (items.isEmpty()) {
                continue;
            }

            List<String> partNumbers = items.stream()
                    .map(InventoryItem::getName)
                    .collect(Collectors.toList());

            try {
                MonitoringPriceRequest request = new MonitoringPriceRequest(
                        supplier.getExternalSupplierId(), partNumbers);
                MonitoringPriceResponse response = monitoringClient.fetchPrices(request);

                if (response != null && response.getItems() != null) {
                    for (MonitoringPriceResponse.PriceItem priceItem : response.getItems()) {
                        // обновляем цену для соответствующих товаров
                        for (InventoryItem item : items) {
                            if (item.getName().equalsIgnoreCase(priceItem.getItemName())) {
                                item.setCurrentMarketPrice(priceItem.getPrice());
                                item.setPriceUpdatedAt(LocalDateTime.now());
                                inventoryItemRepository.save(item);
                            }
                        }
                    }
                }

                log.info("Обновлены цены от поставщика '{}'", supplier.getName());
            } catch (Exception e) {
                log.error("Ошибка обновления цен от поставщика '{}': {}", supplier.getName(), e.getMessage());
            }
        }
    }
}
