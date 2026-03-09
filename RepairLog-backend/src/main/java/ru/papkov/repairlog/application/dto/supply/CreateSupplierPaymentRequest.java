package ru.papkov.repairlog.application.dto.supply;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO для записи оплаты поставщику.
 */
public class CreateSupplierPaymentRequest {

    @NotNull(message = "ID заявки обязателен")
    private Long supplyRequestId;

    @NotNull(message = "Сумма оплаты обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal paidAmount;

    @NotBlank(message = "Способ оплаты обязателен")
    private String paymentMethod;

    private String transactionId;

    private String comment;

    // ========== Getters / Setters ==========

    public Long getSupplyRequestId() { return supplyRequestId; }
    public void setSupplyRequestId(Long supplyRequestId) { this.supplyRequestId = supplyRequestId; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
