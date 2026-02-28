package ru.papkov.repairlog.application.dto.monitoring;

import java.time.LocalDateTime;

/**
 * DTO входящего вебхука от Сервиса Мониторинга со статусом заказа.
 */
public class OrderStatusWebhookRequest {

    private String externalOrderId;
    private String status;
    private String trackingNumber;
    private LocalDateTime estimatedDelivery;
    private String message;

    public OrderStatusWebhookRequest() {}

    public String getExternalOrderId() { return externalOrderId; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public LocalDateTime getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
