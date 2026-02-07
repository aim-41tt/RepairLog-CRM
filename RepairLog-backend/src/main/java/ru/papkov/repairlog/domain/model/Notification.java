package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


import java.time.LocalDateTime;

/**
 *  класс для очереди уведомлений клиентов.
 * Отслеживает статус отправки SMS, Email и Push уведомлений.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_client", columnList = "client_id"),
    @Index(name = "idx_notifications_status", columnList = "status"),
    @Index(name = "idx_notifications_retry", columnList = "retry_count, last_attempt")
})


public class Notification extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
     * Клиент, которому отправляется уведомление.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * Связанный заказ на ремонт (опционально).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_order_id")
    private RepairOrder repairOrder;

    /**
     * Тип уведомления.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 20)
    private NotificationType notificationType;

    /**
     * Текст сообщения.
     */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Дата и время отправки.
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * Статус отправки.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    
    private NotificationStatus status = NotificationStatus.PENDING;

    /**
     * Сообщение об ошибке (если отправка не удалась).
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Количество попыток отправки.
     */
    @Column(name = "retry_count", nullable = false)
    
    private Integer retryCount = 0;

    /**
     * Время последней попытки отправки.
     */
    @Column(name = "last_attempt")
    private LocalDateTime lastAttempt;

    /**
     * Типы уведомлений.
     */
    public enum NotificationType {
        SMS,    // SMS сообщение
        EMAIL,  // Email письмо
        PUSH    // Push уведомление
    }

    /**
     * Статусы уведомлений.
     */
    public enum NotificationStatus {
        PENDING,    // Ожидает отправки
        SENT,       // Успешно отправлено
        FAILED,     // Не удалось отправить
        CANCELLED   // Отменено
    }

    /**
     * Отметить уведомление как отправленное.
     */
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Отметить уведомление как неудачное.
     */
    public void markAsFailed(String error) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = error;
        this.retryCount++;
        this.lastAttempt = LocalDateTime.now();
    }

    /**
     * Проверить, можно ли повторить отправку.
     */
    public boolean canRetry(int maxRetries) {
        return retryCount < maxRetries && NotificationStatus.FAILED.equals(status);
    }

    /**
	 * @return the client
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * @param client the client to set
	 */
	public void setClient(Client client) {
		this.client = client;
	}

	/**
	 * @return the repairOrder
	 */
	public RepairOrder getRepairOrder() {
		return repairOrder;
	}

	/**
	 * @param repairOrder the repairOrder to set
	 */
	public void setRepairOrder(RepairOrder repairOrder) {
		this.repairOrder = repairOrder;
	}

	/**
	 * @return the notificationType
	 */
	public NotificationType getNotificationType() {
		return notificationType;
	}

	/**
	 * @param notificationType the notificationType to set
	 */
	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the sentAt
	 */
	public LocalDateTime getSentAt() {
		return sentAt;
	}

	/**
	 * @param sentAt the sentAt to set
	 */
	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}

	/**
	 * @return the status
	 */
	public NotificationStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(NotificationStatus status) {
		this.status = status;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the retryCount
	 */
	public Integer getRetryCount() {
		return retryCount;
	}

	/**
	 * @param retryCount the retryCount to set
	 */
	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	/**
	 * @return the lastAttempt
	 */
	public LocalDateTime getLastAttempt() {
		return lastAttempt;
	}

	/**
	 * @param lastAttempt the lastAttempt to set
	 */
	public void setLastAttempt(LocalDateTime lastAttempt) {
		this.lastAttempt = lastAttempt;
	}

	@Override
    public String toString() {
        return "Notification{" +
            "id=" + getId() +
            ", clientId=" + (client != null ? client.getId() : null) +
            ", type=" + notificationType +
            ", status=" + status +
            ", retryCount=" + retryCount +
            '}';
    }
}
