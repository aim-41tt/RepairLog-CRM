package ru.papkov.repairlog.application.dto.receipt;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Запрос на создание платежа.
 *
 * @author aim-41tt
 */
public class CreatePaymentRequest {

    @NotNull(message = "ID чека обязателен")
    private Long receiptId;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal paidAmount;

    @NotBlank(message = "Способ оплаты обязателен")
    private String paymentMethod;

    private String transactionId;

    public CreatePaymentRequest() {}

    public Long getReceiptId() { return receiptId; }
    public void setReceiptId(Long receiptId) { this.receiptId = receiptId; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
