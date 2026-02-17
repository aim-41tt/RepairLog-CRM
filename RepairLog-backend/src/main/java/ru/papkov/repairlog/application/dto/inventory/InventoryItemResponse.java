package ru.papkov.repairlog.application.dto.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO ответа со складской позицией.
 *
 * @author aim-41tt
 */
public class InventoryItemResponse {
    private Long id;
    private String name;
    private String serialNumber;
    private String degreeWearName;
    private boolean device;
    private BigDecimal unitPrice;
    private int quantity;
    private boolean inStock;
    private int minStockLevel;
    private String stockStatus;
    private LocalDateTime createdAt;

    public InventoryItemResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getDegreeWearName() { return degreeWearName; }
    public void setDegreeWearName(String degreeWearName) { this.degreeWearName = degreeWearName; }
    public boolean isDevice() { return device; }
    public void setDevice(boolean device) { this.device = device; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }
    public int getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(int minStockLevel) { this.minStockLevel = minStockLevel; }
    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
