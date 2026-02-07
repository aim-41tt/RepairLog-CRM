package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


import java.math.BigDecimal;

/**
 *  класс для запчастей, использованных в ремонтных работах.
 * Composite key (repair_work_id, inventory_item_id).
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "repair_work_items", indexes = {
    @Index(name = "idx_repair_work_items_work", columnList = "repair_work_id"),
    @Index(name = "idx_repair_work_items_item", columnList = "inventory_item_id")
})
@IdClass(RepairWorkItemId.class)
public class RepairWorkItem {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_work_id", nullable = false)
    private RepairWork repairWork;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "quantity", nullable = false)
    
    private Integer quantity = 1;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /**
	 * @return the repairWork
	 */
	public RepairWork getRepairWork() {
		return repairWork;
	}

	/**
	 * @param repairWork the repairWork to set
	 */
	public void setRepairWork(RepairWork repairWork) {
		this.repairWork = repairWork;
	}

	/**
	 * @return the inventoryItem
	 */
	public InventoryItem getInventoryItem() {
		return inventoryItem;
	}

	/**
	 * @param inventoryItem the inventoryItem to set
	 */
	public void setInventoryItem(InventoryItem inventoryItem) {
		this.inventoryItem = inventoryItem;
	}

	/**
	 * @return the quantity
	 */
	public Integer getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the price
	 */
	public BigDecimal getPrice() {
		return price;
	}

	/**
	 * @param price the price to set
	 */
	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Override
    public String toString() {
        return "RepairWorkItem{" +
            "workId=" + (repairWork != null ? repairWork.getId() : null) +
            ", itemId=" + (inventoryItem != null ? inventoryItem.getId() : null) +
            ", quantity=" + quantity +
            ", price=" + price +
            '}';
    }
}
