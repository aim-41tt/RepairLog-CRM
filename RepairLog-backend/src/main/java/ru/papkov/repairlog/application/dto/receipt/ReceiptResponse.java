package ru.papkov.repairlog.application.dto.receipt;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO ответа с данными чека.
 *
 * @author aim-41tt
 */
public class ReceiptResponse {
    private Long id;
    private Long repairOrderId;
    private String orderNumber;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private boolean locked;
    private LocalDateTime createdAt;
    private List<RepairWorkResponse> works;
    private List<PaymentResponse> payments;

    public ReceiptResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRepairOrderId() { return repairOrderId; }
    public void setRepairOrderId(Long repairOrderId) { this.repairOrderId = repairOrderId; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<RepairWorkResponse> getWorks() { return works; }
    public void setWorks(List<RepairWorkResponse> works) { this.works = works; }
    public List<PaymentResponse> getPayments() { return payments; }
    public void setPayments(List<PaymentResponse> payments) { this.payments = payments; }

    /** Вложенный DTO для работ в чеке. */
    public static class RepairWorkResponse {
        private Long id;
        private String description;
        private BigDecimal price;
        private String employeeName;
        private LocalDateTime completedAt;

        public RepairWorkResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    }

    /** Вложенный DTO для платежей чека. */
    public static class PaymentResponse {
        private Long id;
        private BigDecimal paidAmount;
        private String paymentMethod;
        private LocalDateTime paidAt;
        private String acceptedByName;

        public PaymentResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public BigDecimal getPaidAmount() { return paidAmount; }
        public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public LocalDateTime getPaidAt() { return paidAt; }
        public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
        public String getAcceptedByName() { return acceptedByName; }
        public void setAcceptedByName(String acceptedByName) { this.acceptedByName = acceptedByName; }
    }
}
