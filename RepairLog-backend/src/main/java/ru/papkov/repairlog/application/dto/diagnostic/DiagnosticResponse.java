package ru.papkov.repairlog.application.dto.diagnostic;

import java.time.LocalDateTime;

/**
 * DTO ответа с данными диагностики.
 *
 * @author aim-41tt
 */
public class DiagnosticResponse {
    private Long id;
    private Long repairOrderId;
    private String orderNumber;
    private String description;
    private String solution;
    private String performedByName;
    private LocalDateTime createdAt;

    public DiagnosticResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRepairOrderId() { return repairOrderId; }
    public void setRepairOrderId(Long repairOrderId) { this.repairOrderId = repairOrderId; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }
    public String getPerformedByName() { return performedByName; }
    public void setPerformedByName(String performedByName) { this.performedByName = performedByName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
