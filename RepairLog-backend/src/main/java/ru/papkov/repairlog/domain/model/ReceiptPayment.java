package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 *  класс для платежей по чекам.
 * Поддерживает частичную оплату и различные способы оплаты.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "receipt_payments", indexes = {
    @Index(name = "idx_receipt_payments_receipt", columnList = "receipt_id"),
    @Index(name = "idx_receipt_payments_transaction", columnList = "transaction_id")
})


public class ReceiptPayment extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	/**
     * Чек, по которому проводится оплата.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    private Receipt receipt;

    /**
     * Сумма платежа.
     */
    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount;

    /**
     * Способ оплаты.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 8)
    private PaymentMethod paymentMethod;

    /**
     * Дата и время оплаты.
     */
    @Column(name = "paid_at", nullable = false)
    
    private LocalDateTime paidAt = LocalDateTime.now();

    /**
     * Сотрудник, принявший оплату (RECEPTIONIST).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_by", nullable = false)
    private Employee acceptedBy;

    /**
     * ID транзакции от платежной системы (терминал, эквайринг).
     */
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    /**
     * Дополнительные детали платежа в формате JSON.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_details", columnDefinition = "jsonb")
    private Map<String, Object> paymentDetails;

    /**
     * Способы оплаты.
     */
    public enum PaymentMethod {
        CASH,      // Наличные
        CARD,      // Банковская карта
        TRANSFER,  // Банковский перевод
        OTHER      // Другой способ
    }

    /**
	 * @return the receipt
	 */
	public Receipt getReceipt() {
		return receipt;
	}

	/**
	 * @param receipt the receipt to set
	 */
	public void setReceipt(Receipt receipt) {
		this.receipt = receipt;
	}

	/**
	 * @return the paidAmount
	 */
	public BigDecimal getPaidAmount() {
		return paidAmount;
	}

	/**
	 * @param paidAmount the paidAmount to set
	 */
	public void setPaidAmount(BigDecimal paidAmount) {
		this.paidAmount = paidAmount;
	}

	/**
	 * @return the paymentMethod
	 */
	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	/**
	 * @param paymentMethod the paymentMethod to set
	 */
	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	/**
	 * @return the paidAt
	 */
	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	/**
	 * @param paidAt the paidAt to set
	 */
	public void setPaidAt(LocalDateTime paidAt) {
		this.paidAt = paidAt;
	}

	/**
	 * @return the acceptedBy
	 */
	public Employee getAcceptedBy() {
		return acceptedBy;
	}

	/**
	 * @param acceptedBy the acceptedBy to set
	 */
	public void setAcceptedBy(Employee acceptedBy) {
		this.acceptedBy = acceptedBy;
	}

	/**
	 * @return the transactionId
	 */
	public String getTransactionId() {
		return transactionId;
	}

	/**
	 * @param transactionId the transactionId to set
	 */
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	/**
	 * @return the paymentDetails
	 */
	public Map<String, Object> getPaymentDetails() {
		return paymentDetails;
	}

	/**
	 * @param paymentDetails the paymentDetails to set
	 */
	public void setPaymentDetails(Map<String, Object> paymentDetails) {
		this.paymentDetails = paymentDetails;
	}

	@Override
    public String toString() {
        return "ReceiptPayment{" +
            "id=" + getId() +
            ", receiptId=" + (receipt != null ? receipt.getId() : null) +
            ", paidAmount=" + paidAmount +
            ", paymentMethod=" + paymentMethod +
            ", paidAt=" + paidAt +
            '}';
    }
}
