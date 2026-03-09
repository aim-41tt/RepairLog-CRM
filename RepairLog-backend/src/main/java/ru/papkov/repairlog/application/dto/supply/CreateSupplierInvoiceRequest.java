package ru.papkov.repairlog.application.dto.supply;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO для привязки счёта от поставщика к заявке на поставку.
 */
public class CreateSupplierInvoiceRequest {

    @NotNull(message = "ID заявки обязателен")
    private Long supplyRequestId;

    @NotBlank(message = "Номер счёта обязателен")
    private String invoiceNumber;

    @NotNull(message = "Дата счёта обязательна")
    private LocalDate invoiceDate;

    @NotNull(message = "Сумма счёта обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal totalAmount;

    private LocalDate dueDate;

    // ========== Getters / Setters ==========

    public Long getSupplyRequestId() { return supplyRequestId; }
    public void setSupplyRequestId(Long supplyRequestId) { this.supplyRequestId = supplyRequestId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
