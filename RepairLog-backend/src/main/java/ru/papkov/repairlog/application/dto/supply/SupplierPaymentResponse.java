package ru.papkov.repairlog.application.dto.supply;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO ответа с информацией об оплате поставщику.
 */
public class SupplierPaymentResponse {

    private Long id;
    private Long supplyRequestId;
    private String requestNumber;
    private BigDecimal paidAmount;
    private String paymentMethod;
    private LocalDateTime paidAt;
    private String paidByName;
    private String transactionId;
    private String comment;

    // ========== Getters / Setters ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSupplyRequestId() { return supplyRequestId; }
    public void setSupplyRequestId(Long supplyRequestId) { this.supplyRequestId = supplyRequestId; }

    public String getRequestNumber() { return requestNumber; }
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public String getPaidByName() { return paidByName; }
    public void setPaidByName(String paidByName) { this.paidByName = paidByName; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
