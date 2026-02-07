package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


import java.time.LocalDateTime;

/**
 *  класс для истории изменения статусов заказов на ремонт.
 * Позволяет отслеживать весь жизненный цикл заказа.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "status_history", indexes = {
    @Index(name = "idx_status_history_order", columnList = "repair_order_id"),
    @Index(name = "idx_status_history_status", columnList = "status_id"),
    @Index(name = "idx_status_history_date", columnList = "changed_at")
})


public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Заказ на ремонт.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_order_id", nullable = false)
    private RepairOrder repairOrder;

    /**
     * Новый статус заказа.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private RepairStatus status;

    /**
     * Дата и время изменения статуса.
     */
    @Column(name = "changed_at", nullable = false)
    
    private LocalDateTime changedAt = LocalDateTime.now();

    /**
     * Сотрудник, изменивший статус.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private Employee changedBy;

    /**
     * Комментарий к изменению статуса.
     */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    /**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
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
	 * @return the status
	 */
	public RepairStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(RepairStatus status) {
		this.status = status;
	}

	/**
	 * @return the changedAt
	 */
	public LocalDateTime getChangedAt() {
		return changedAt;
	}

	/**
	 * @param changedAt the changedAt to set
	 */
	public void setChangedAt(LocalDateTime changedAt) {
		this.changedAt = changedAt;
	}

	/**
	 * @return the changedBy
	 */
	public Employee getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy the changedBy to set
	 */
	public void setChangedBy(Employee changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
    public String toString() {
        return "StatusHistory{" +
            "id=" + id +
            ", orderId=" + (repairOrder != null ? repairOrder.getId() : null) +
            ", status='" + (status != null ? status.getName() : null) + '\'' +
            ", changedAt=" + changedAt +
            '}';
    }
}
