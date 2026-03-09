package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Сущность счёта от поставщика.
 * Привязывается к заявке на поставку для отслеживания финансовых документов.
 *
 * @author aim-41tt
 */
@Entity
@Table(name = "supplier_invoices", indexes = {
    @Index(name = "idx_supplier_invoices_request", columnList = "supply_request_id"),
    @Index(name = "idx_supplier_invoices_supplier", columnList = "supplier_id")
})
public class SupplierInvoice extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Заявка на поставку, к которой привязан счёт.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_request_id", nullable = false)
    private SupplyRequest supplyRequest;

    /**
     * Поставщик, выставивший счёт.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    /**
     * Номер счёта.
     */
    @Column(name = "invoice_number", nullable = false, length = 50)
    private String invoiceNumber;

    /**
     * Дата выставления счёта.
     */
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    /**
     * Сумма по счёту.
     */
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Срок оплаты.
     */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /**
     * Статус счёта.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.PENDING;

    /**
     * Статусы счёта от поставщика.
     */
    public enum InvoiceStatus {
        PENDING,     // Ожидает оплаты
        PAID,        // Оплачен
        OVERDUE,     // Просрочен
        CANCELLED    // Отменён
    }

    // ========== Getters / Setters ==========

    public SupplyRequest getSupplyRequest() {
        return supplyRequest;
    }

    public void setSupplyRequest(SupplyRequest supplyRequest) {
        this.supplyRequest = supplyRequest;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SupplierInvoice{" +
            "id=" + getId() +
            ", invoiceNumber='" + invoiceNumber + '\'' +
            ", totalAmount=" + totalAmount +
            ", status=" + status +
            '}';
    }
}
