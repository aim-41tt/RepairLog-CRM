package ru.papkov.repairlog.application.dto.monitoring;

/**
 * DTO ответа от Сервиса Мониторинга при создании заказа.
 */
public class MonitoringOrderResponse {

    private String externalOrderId;
    private String status;
    private String message;

    public MonitoringOrderResponse() {}

    public String getExternalOrderId() { return externalOrderId; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
