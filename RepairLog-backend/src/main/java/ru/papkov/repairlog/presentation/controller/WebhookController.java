package ru.papkov.repairlog.presentation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.papkov.repairlog.application.dto.monitoring.OrderStatusWebhookRequest;
import ru.papkov.repairlog.application.dto.monitoring.PriceUpdateWebhookRequest;
import ru.papkov.repairlog.application.service.SupplyRequestService;
import ru.papkov.repairlog.domain.model.InventoryItem;
import ru.papkov.repairlog.domain.model.Supplier;
import ru.papkov.repairlog.domain.repository.InventoryItemRepository;
import ru.papkov.repairlog.domain.repository.SupplierRepository;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Контроллер для приёма вебхуков от внешнего Сервиса Мониторинга.
 * Доступ без авторизации (настраивается в SecurityConfig).
 */
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final InventoryItemRepository inventoryItemRepository;
    private final SupplierRepository supplierRepository;
    private final SupplyRequestService supplyRequestService;

    public WebhookController(InventoryItemRepository inventoryItemRepository,
                              SupplierRepository supplierRepository,
                              SupplyRequestService supplyRequestService) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.supplierRepository = supplierRepository;
        this.supplyRequestService = supplyRequestService;
    }

    /**
     * Вебхук обновления цен от Сервиса Мониторинга.
     */
    @PostMapping("/prices")
    public ResponseEntity<Map<String, String>> handlePriceUpdate(@RequestBody PriceUpdateWebhookRequest request) {
        log.info("Получен вебхук обновления цен от поставщика {}", request.getSupplierId());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Пустой список позиций"));
        }

        int updated = 0;
        for (PriceUpdateWebhookRequest.PriceItem priceItem : request.getItems()) {
            // ищем товар по наименованию
            var items = inventoryItemRepository.findByNameContainingIgnoreCase(priceItem.getItemName());
            for (InventoryItem item : items) {
                item.setCurrentMarketPrice(priceItem.getPrice());
                item.setPriceUpdatedAt(LocalDateTime.now());
                inventoryItemRepository.save(item);
                updated++;
            }
        }

        log.info("Обновлено {} позиций по ценам", updated);
        return ResponseEntity.ok(Map.of("status", "ok", "updated", String.valueOf(updated)));
    }

    /**
     * Вебхук статуса заказа от Сервиса Мониторинга.
     */
    @PostMapping("/order-status")
    public ResponseEntity<Map<String, String>> handleOrderStatus(@RequestBody OrderStatusWebhookRequest request) {
        log.info("Получен вебхук статуса заказа: externalOrderId={}, status={}",
                request.getExternalOrderId(), request.getStatus());

        if (request.getExternalOrderId() == null) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "externalOrderId обязателен"));
        }

        supplyRequestService.updateExternalOrderInfo(request.getExternalOrderId(), request.getStatus());

        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
