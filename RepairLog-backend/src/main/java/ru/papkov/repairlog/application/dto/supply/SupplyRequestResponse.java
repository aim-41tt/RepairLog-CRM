package ru.papkov.repairlog.application.dto.supply;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO ответа с данными заявки на поставку.
 *
 * @author aim-41tt
 */
public class SupplyRequestResponse {

    private Long id;
    private String requestNumber;
    private String supplierName;
    private Long supplierId;
    private String statusName;
    private String requestedByName;
    private String approvedByName;
    private BigDecimal totalAmount;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime expectedDeliveryDate;
    private List<SupplyRequestItemResponse> items;
    private Long relatedRepairOrderId;
    private String relatedOrderNumber;
    private String source;
    private String externalOrderId;
    private String externalOrderStatus;

    public SupplyRequestResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestNumber() { return requestNumber; }
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public String getRequestedByName() { return requestedByName; }
    public void setRequestedByName(String requestedByName) { this.requestedByName = requestedByName; }
    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDateTime expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
    public List<SupplyRequestItemResponse> getItems() { return items; }
    public void setItems(List<SupplyRequestItemResponse> items) { this.items = items; }
    public Long getRelatedRepairOrderId() { return relatedRepairOrderId; }
    public void setRelatedRepairOrderId(Long relatedRepairOrderId) { this.relatedRepairOrderId = relatedRepairOrderId; }
    public String getRelatedOrderNumber() { return relatedOrderNumber; }
    public void setRelatedOrderNumber(String relatedOrderNumber) { this.relatedOrderNumber = relatedOrderNumber; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getExternalOrderId() { return externalOrderId; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }
    public String getExternalOrderStatus() { return externalOrderStatus; }
    public void setExternalOrderStatus(String externalOrderStatus) { this.externalOrderStatus = externalOrderStatus; }
}
