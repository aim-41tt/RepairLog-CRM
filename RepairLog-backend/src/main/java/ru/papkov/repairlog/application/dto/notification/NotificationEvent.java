package ru.papkov.repairlog.application.dto.notification;

import java.time.LocalDateTime;

/**
 * DTO события уведомления для отправки в Kafka.
 * Внешний сервис уведомлений получает эти события и отправляет SMS/Email клиенту.
 *
 * @author aim-41tt
 */
public class NotificationEvent {

    private Long notificationId;
    private Long clientId;
    private String clientPhone;
    private String clientEmail;
    private String clientFullName;
    private String notificationType;
    private String message;
    private Long repairOrderId;
    private String orderNumber;
    private LocalDateTime createdAt;

    public NotificationEvent() {}

    // ========== Getters / Setters ==========

    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }

    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }

    public String getClientFullName() { return clientFullName; }
    public void setClientFullName(String clientFullName) { this.clientFullName = clientFullName; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getRepairOrderId() { return repairOrderId; }
    public void setRepairOrderId(Long repairOrderId) { this.repairOrderId = repairOrderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "NotificationEvent{" +
            "notificationId=" + notificationId +
            ", clientId=" + clientId +
            ", notificationType='" + notificationType + '\'' +
            ", repairOrderId=" + repairOrderId +
            '}';
    }
}
