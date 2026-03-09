package ru.papkov.repairlog.application.dto.supply;

import java.math.BigDecimal;

/**
 * DTO элемента заявки на поставку.
 *
 * @author aim-41tt
 */
public class SupplyRequestItemResponse {

    private Long id;
    private String itemName;
    private String partNumber;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    /** ID складской позиции (если позиция привязана к складу). Null — если позиции нет на складе. */
    private Long inventoryItemId;
    /** Название складской позиции (для удобства фронтенда). */
    private String inventoryItemName;

    public SupplyRequestItemResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public Long getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(Long inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    public String getInventoryItemName() { return inventoryItemName; }
    public void setInventoryItemName(String inventoryItemName) { this.inventoryItemName = inventoryItemName; }
}
