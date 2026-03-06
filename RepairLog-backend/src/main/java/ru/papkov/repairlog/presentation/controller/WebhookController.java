package ru.papkov.repairlog.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.papkov.repairlog.application.dto.monitoring.OrderStatusWebhookRequest;
import ru.papkov.repairlog.application.dto.monitoring.PriceUpdateWebhookRequest;
import ru.papkov.repairlog.application.service.InventoryService;
import ru.papkov.repairlog.application.service.SupplyRequestService;
import ru.papkov.repairlog.domain.repository.SupplierRepository;
import ru.papkov.repairlog.infrastructure.security.webhook.WebhookSignatureValidator;

import java.time.Duration;
import java.util.Map;

/**
 * Контроллер для приёма вебхуков от внешнего Сервиса Мониторинга.
 * Доступ без авторизации (настраивается в SecurityConfig).
 *
 * Безопасность:
 * - HMAC-SHA256 подпись через заголовок X-Webhook-Signature
 * - Идемпотентность через заголовок X-Idempotency-Key (Redis, TTL 24 часа)
 */
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final InventoryService inventoryService;
    private final SupplyRequestService supplyRequestService;
    private final WebhookSignatureValidator signatureValidator;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public WebhookController(InventoryService inventoryService,
                              SupplierRepository supplierRepository,
                              SupplyRequestService supplyRequestService,
                              WebhookSignatureValidator signatureValidator,
                              StringRedisTemplate redisTemplate,
                              ObjectMapper objectMapper) {
        this.inventoryService = inventoryService;
        this.supplyRequestService = supplyRequestService;
        this.signatureValidator = signatureValidator;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Вебхук обновления цен от Сервиса Мониторинга.
     */
    @PostMapping("/prices")
    public ResponseEntity<Map<String, String>> handlePriceUpdate(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        if (!signatureValidator.isValid(rawBody, signature)) {
            log.warn("Невалидная подпись вебхука /prices");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Invalid signature"));
        }

        if (isDuplicate(idempotencyKey)) {
            log.info("Дубликат вебхука /prices с ключом {}", idempotencyKey);
            return ResponseEntity.ok(Map.of("status", "duplicate"));
        }

        try {
            PriceUpdateWebhookRequest request = objectMapper.readValue(rawBody, PriceUpdateWebhookRequest.class);

            log.info("Получен вебхук обновления цен от поставщика {}", request.getSupplierId());

            if (request.getItems() == null || request.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Пустой список позиций"));
            }

            int updated = inventoryService.updatePricesFromWebhook(request.getItems());

            return ResponseEntity.ok(Map.of("status", "ok", "updated", String.valueOf(updated)));
        } catch (Exception e) {
            log.error("Ошибка обработки вебхука /prices", e);
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", errorMsg));
        }
    }

    /**
     * Вебхук статуса заказа от Сервиса Мониторинга.
     */
    @PostMapping("/order-status")
    public ResponseEntity<Map<String, String>> handleOrderStatus(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        if (!signatureValidator.isValid(rawBody, signature)) {
            log.warn("Невалидная подпись вебхука /order-status");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Invalid signature"));
        }

        if (isDuplicate(idempotencyKey)) {
            log.info("Дубликат вебхука /order-status с ключом {}", idempotencyKey);
            return ResponseEntity.ok(Map.of("status", "duplicate"));
        }

        try {
            OrderStatusWebhookRequest request = objectMapper.readValue(rawBody, OrderStatusWebhookRequest.class);

            log.info("Получен вебхук статуса заказа: externalOrderId={}, status={}",
                    request.getExternalOrderId(), request.getStatus());

            if (request.getExternalOrderId() == null) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "externalOrderId обязателен"));
            }

            supplyRequestService.updateExternalOrderInfo(request.getExternalOrderId(), request.getStatus());

            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("Ошибка обработки вебхука /order-status", e);
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", errorMsg));
        }
    }

    /**
     * Проверка идемпотентности через Redis.
     * Ключ хранится 24 часа.
     */
    private boolean isDuplicate(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return false;
        }
        Boolean wasAbsent = redisTemplate.opsForValue()
                .setIfAbsent("webhook:idem:" + idempotencyKey, "1", Duration.ofHours(24));
        return !Boolean.TRUE.equals(wasAbsent);
    }
}
