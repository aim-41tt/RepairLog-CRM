package ru.papkov.repairlog.application.dto.supply;

import java.math.BigDecimal;

/**
 * DTO для дашборда управления поставками.
 */
public class SupplyDashboardResponse {

    private long totalActiveRequests;
    private long pendingApprovalCount;
    private long autoFormedCount;
    private long orderedCount;
    private long inTransitCount;
    private long overdueCount;
    private long lowStockItemsCount;
    private long outOfStockItemsCount;
    private BigDecimal totalPendingAmount;

    public SupplyDashboardResponse() {}

    public long getTotalActiveRequests() { return totalActiveRequests; }
    public void setTotalActiveRequests(long totalActiveRequests) { this.totalActiveRequests = totalActiveRequests; }
    public long getPendingApprovalCount() { return pendingApprovalCount; }
    public void setPendingApprovalCount(long pendingApprovalCount) { this.pendingApprovalCount = pendingApprovalCount; }
    public long getAutoFormedCount() { return autoFormedCount; }
    public void setAutoFormedCount(long autoFormedCount) { this.autoFormedCount = autoFormedCount; }
    public long getOrderedCount() { return orderedCount; }
    public void setOrderedCount(long orderedCount) { this.orderedCount = orderedCount; }
    public long getInTransitCount() { return inTransitCount; }
    public void setInTransitCount(long inTransitCount) { this.inTransitCount = inTransitCount; }
    public long getOverdueCount() { return overdueCount; }
    public void setOverdueCount(long overdueCount) { this.overdueCount = overdueCount; }
    public long getLowStockItemsCount() { return lowStockItemsCount; }
    public void setLowStockItemsCount(long lowStockItemsCount) { this.lowStockItemsCount = lowStockItemsCount; }
    public long getOutOfStockItemsCount() { return outOfStockItemsCount; }
    public void setOutOfStockItemsCount(long outOfStockItemsCount) { this.outOfStockItemsCount = outOfStockItemsCount; }
    public BigDecimal getTotalPendingAmount() { return totalPendingAmount; }
    public void setTotalPendingAmount(BigDecimal totalPendingAmount) { this.totalPendingAmount = totalPendingAmount; }
}
