package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


import java.math.BigDecimal;

/**
 *  класс для позиций в заявках на поставку.
 * Каждая позиция указывает товар и запрашиваемое количество.
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
     * Складской товар.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    /**
     * Запрашиваемое количество.
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Ожидаемая цена за единицу (может быть null если неизвестна).
     */
    @Column(name = "expected_price", precision = 12, scale = 2)
    private BigDecimal expectedPrice;

    /**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the supplyRequest
	 */
	public SupplyRequest getSupplyRequest() {
		return supplyRequest;
	}

	/**
	 * @param supplyRequest the supplyRequest to set
	 */
	public void setSupplyRequest(SupplyRequest supplyRequest) {
		this.supplyRequest = supplyRequest;
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
	 * @return the expectedPrice
	 */
	public BigDecimal getExpectedPrice() {
		return expectedPrice;
	}

	/**
	 * @param expectedPrice the expectedPrice to set
	 */
	public void setExpectedPrice(BigDecimal expectedPrice) {
		this.expectedPrice = expectedPrice;
	}

	@Override
    public String toString() {
        return "SupplyRequestItem{" +
            "id=" + id +
            ", supplyRequestId=" + (supplyRequest != null ? supplyRequest.getId() : null) +
            ", inventoryItemId=" + (inventoryItem != null ? inventoryItem.getId() : null) +
            ", quantity=" + quantity +
            ", expectedPrice=" + expectedPrice +
            '}';
    }
}
