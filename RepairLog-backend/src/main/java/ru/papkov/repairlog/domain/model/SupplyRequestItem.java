package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Сущность позиции в заявке на поставку.
 * Каждая позиция указывает наименование, артикул, количество и цену.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "supply_request_items")
public class SupplyRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Заявка на поставку.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_request_id", nullable = false)
    private SupplyRequest supplyRequest;

    /**
     * Связанная складская позиция (опционально, может быть null если товар ещё не на складе).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id")
    private InventoryItem inventoryItem;

    /**
     * Наименование запрашиваемой позиции.
     */
    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    /**
     * Артикул / каталожный номер.
     */
    @Column(name = "part_number", length = 100)
    private String partNumber;

    /**
     * Запрашиваемое количество.
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Цена за единицу.
     */
    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Общая стоимость позиции (quantity * unitPrice).
     */
    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;

    // ========== Getters / Setters ==========

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public SupplyRequest getSupplyRequest() { return supplyRequest; }
	public void setSupplyRequest(SupplyRequest supplyRequest) { this.supplyRequest = supplyRequest; }
	public InventoryItem getInventoryItem() { return inventoryItem; }
	public void setInventoryItem(InventoryItem inventoryItem) { this.inventoryItem = inventoryItem; }
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

	@Override
    public String toString() {
        return "SupplyRequestItem{" +
            "id=" + id +
            ", itemName='" + itemName + '\'' +
            ", quantity=" + quantity +
            ", unitPrice=" + unitPrice +
            '}';
    }
}
