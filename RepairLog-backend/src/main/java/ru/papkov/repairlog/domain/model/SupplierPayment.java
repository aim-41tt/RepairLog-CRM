package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;
import ru.papkov.repairlog.domain.model.enums.SupplierPaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность оплаты поставщику.
 * Фиксирует факт оплаты по заявке на поставку после приёмки товара.
 *
 * @author aim-41tt
 */
@Entity
@Table(name = "supplier_payments", indexes = {
    @Index(name = "idx_supplier_payments_request", columnList = "supply_request_id"),
    @Index(name = "idx_supplier_payments_transaction", columnList = "transaction_id")
})
public class SupplierPayment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Заявка на поставку, по которой проводится оплата.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_request_id", nullable = false)
    private SupplyRequest supplyRequest;

    /**
     * Сумма оплаты.
     */
    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount;

    /**
     * Способ оплаты.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private SupplierPaymentMethod paymentMethod;

    /**
     * Дата и время оплаты.
     */
    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt = LocalDateTime.now();

    /**
     * Сотрудник, проведший оплату (ADMIN).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by", nullable = false)
    private Employee paidBy;

    /**
     * Номер платёжного поручения / ID транзакции.
     */
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    /**
     * Комментарий к оплате.
     */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    // ========== Getters / Setters ==========

    public SupplyRequest getSupplyRequest() {
        return supplyRequest;
    }

    public void setSupplyRequest(SupplyRequest supplyRequest) {
        this.supplyRequest = supplyRequest;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public SupplierPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(SupplierPaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public Employee getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(Employee paidBy) {
        this.paidBy = paidBy;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "SupplierPayment{" +
            "id=" + getId() +
            ", supplyRequestId=" + (supplyRequest != null ? supplyRequest.getId() : null) +
            ", paidAmount=" + paidAmount +
            ", paymentMethod=" + paymentMethod +
            ", paidAt=" + paidAt +
            '}';
    }
}
