package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 *  класс для чеков заказов на ремонт.
 * Один заказ имеет один чек (one-to-one).
 * Суммы пересчитываются автоматически через триггеры в БД.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "receipts", indexes = {
    @Index(name = "idx_receipts_order", columnList = "repair_order_id")
})
public class Receipt extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
     * Заказ на ремонт (one-to-one).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_order_id", nullable = false, unique = true)
    private RepairOrder repairOrder;

    /**
     * Промежуточная сумма (работы + запчасти) без налога и скидки.
     */
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    
    private BigDecimal subtotal = BigDecimal.ZERO;

    /**
     * Сумма скидки.
     */
    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * Сумма налога.
     */
    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /**
     * Итоговая сумма к оплате.
     */
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * Налоговая ставка.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_rate_id")
    private TaxRate taxRate;

    /**
     * Тип скидки.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_type_id")
    private DiscountType discountType;

    /**
     * Статус оплаты.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    /**
     * Флаг блокировки редактирования чека после начала оплаты.
     */
    @Column(name = "locked", nullable = false)
    
    private Boolean locked = false;

    /**
     * Дата и время блокировки чека.
     */
    @Column(name = "locked_at")
    private java.time.LocalDateTime lockedAt;

    /**
     * Сотрудник, заблокировавший чек.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locked_by")
    private Employee lockedBy;

    /**
     * Статусы оплаты чека.
     */
    public enum PaymentStatus {
        UNPAID,           // Не оплачен
        PARTIALLY_PAID,   // Частично оплачен
        FULLY_PAID,       // Полностью оплачен
        REFUNDED          // Возвращён
    }

    /**
     * Проверить, можно ли редактировать чек.
     */
    public boolean isEditable() {
        return !locked;
    }

    /**
     * Заблокировать чек от редактирования.
     */
    public void lock(Employee employee) {
        this.locked = true;
        this.lockedAt = java.time.LocalDateTime.now();
        this.lockedBy = employee;
    }

    /**
     * Проверить, оплачен ли чек полностью.
     */
    public boolean isFullyPaid() {
        return PaymentStatus.FULLY_PAID.equals(paymentStatus);
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
	 * @return the subtotal
	 */
	public BigDecimal getSubtotal() {
		return subtotal;
	}

	/**
	 * @param subtotal the subtotal to set
	 */
	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	/**
	 * @return the discountAmount
	 */
	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	/**
	 * @param discountAmount the discountAmount to set
	 */
	public void setDiscountAmount(BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}

	/**
	 * @return the taxAmount
	 */
	public BigDecimal getTaxAmount() {
		return taxAmount;
	}

	/**
	 * @param taxAmount the taxAmount to set
	 */
	public void setTaxAmount(BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
	}

	/**
	 * @return the totalAmount
	 */
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	/**
	 * @param totalAmount the totalAmount to set
	 */
	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	/**
	 * @return the taxRate
	 */
	public TaxRate getTaxRate() {
		return taxRate;
	}

	/**
	 * @param taxRate the taxRate to set
	 */
	public void setTaxRate(TaxRate taxRate) {
		this.taxRate = taxRate;
	}

	/**
	 * @return the discountType
	 */
	public DiscountType getDiscountType() {
		return discountType;
	}

	/**
	 * @param discountType the discountType to set
	 */
	public void setDiscountType(DiscountType discountType) {
		this.discountType = discountType;
	}

	/**
	 * @return the paymentStatus
	 */
	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	/**
	 * @param paymentStatus the paymentStatus to set
	 */
	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	/**
	 * @return the locked
	 */
	public Boolean getLocked() {
		return locked;
	}

	/**
	 * @param locked the locked to set
	 */
	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	/**
	 * @return the lockedAt
	 */
	public java.time.LocalDateTime getLockedAt() {
		return lockedAt;
	}

	/**
	 * @param lockedAt the lockedAt to set
	 */
	public void setLockedAt(java.time.LocalDateTime lockedAt) {
		this.lockedAt = lockedAt;
	}

	/**
	 * @return the lockedBy
	 */
	public Employee getLockedBy() {
		return lockedBy;
	}

	/**
	 * @param lockedBy the lockedBy to set
	 */
	public void setLockedBy(Employee lockedBy) {
		this.lockedBy = lockedBy;
	}

	@Override
    public String toString() {
        return "Receipt{" +
            "id=" + getId() +
            ", orderId=" + (repairOrder != null ? repairOrder.getId() : null) +
            ", totalAmount=" + totalAmount +
            ", paymentStatus=" + paymentStatus +
            ", locked=" + locked +
            '}';
    }
}
