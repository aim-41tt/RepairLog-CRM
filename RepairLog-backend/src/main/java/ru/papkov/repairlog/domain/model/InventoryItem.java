package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *  класс для складских запасов (запчасти и устройства).
 * Поддерживает как серийные номера для уникальных устройств, так и количественный учёт.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "inventory_items", indexes = {
    @Index(name = "idx_inventory_in_stock", columnList = "in_stock")
})
public class InventoryItem extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	/**
     * Название товара.
     */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /**
     * Серийный номер (для уникальных устройств).
     */
    @Column(name = "serial_number", unique = true, length = 100)
    private String serialNumber;

    /**
     * Степень износа товара.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_wear_id", nullable = false)
    private DegreeWear degreeWear;

    /**
     * Флаг: является ли товар устройством (true) или запчастью (false).
     */
    @Column(name = "is_device", nullable = false)
    private Boolean isDevice;

    /**
     * Цена за единицу товара.
     */
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Количество на складе.
     */
    @Column(name = "quantity", nullable = false)
    
    private Integer quantity = 1;

    /**
     * Флаг наличия на складе.
     */
    @Column(name = "in_stock", nullable = false)
    
    private Boolean inStock = true;

    /**
     * Минимальный уровень запаса для контроля критического остатка.
     */
    @Column(name = "min_stock_level")
    private Integer minStockLevel = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_supplier_id")
    private Supplier preferredSupplier;

    @Column(name = "last_purchase_price", precision = 12, scale = 2)
    private BigDecimal lastPurchasePrice;

    @Column(name = "current_market_price", precision = 12, scale = 2)
    private BigDecimal currentMarketPrice;

    @Column(name = "price_updated_at")
    private LocalDateTime priceUpdatedAt;

    @Column(name = "reorder_quantity")
    private Integer reorderQuantity = 0;

    @Column(name = "pack_size", nullable = false)
    private Integer packSize = 1;

    /**
     * Проверить, ниже ли текущее количество минимального уровня.
     */
    public boolean isBelowMinStock() {
        return quantity < minStockLevel;
    }

    /**
     * Уменьшить количество на складе.
     */
    public void decreaseQuantity(Integer amount) {
        if (quantity < amount) {
            throw new IllegalStateException("Недостаточно товара на складе");
        }
        this.quantity -= amount;
        if (this.quantity == 0) {
            this.inStock = false;
        }
    }

    /**
     * Увеличить количество на складе.
     */
    public void increaseQuantity(Integer amount) {
        this.quantity += amount;
        if (this.quantity > 0) {
            this.inStock = true;
        }
    }

    /**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the serialNumber
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * @param serialNumber the serialNumber to set
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	/**
	 * @return the degreeWear
	 */
	public DegreeWear getDegreeWear() {
		return degreeWear;
	}

	/**
	 * @param degreeWear the degreeWear to set
	 */
	public void setDegreeWear(DegreeWear degreeWear) {
		this.degreeWear = degreeWear;
	}

	/**
	 * @return the isDevice
	 */
	public Boolean getIsDevice() {
		return isDevice;
	}

	/**
	 * @param isDevice the isDevice to set
	 */
	public void setIsDevice(Boolean isDevice) {
		this.isDevice = isDevice;
	}

	/**
	 * @return the unitPrice
	 */
	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	/**
	 * @param unitPrice the unitPrice to set
	 */
	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
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
	 * @return the inStock
	 */
	public Boolean getInStock() {
		return inStock;
	}

	/**
	 * @param inStock the inStock to set
	 */
	public void setInStock(Boolean inStock) {
		this.inStock = inStock;
	}

	/**
	 * @return the minStockLevel
	 */
	public Integer getMinStockLevel() {
		return minStockLevel;
	}

	/**
	 * @param minStockLevel the minStockLevel to set
	 */
	public void setMinStockLevel(Integer minStockLevel) {
		this.minStockLevel = minStockLevel;
	}

	public Supplier getPreferredSupplier() { return preferredSupplier; }
	public void setPreferredSupplier(Supplier preferredSupplier) { this.preferredSupplier = preferredSupplier; }
	public BigDecimal getLastPurchasePrice() { return lastPurchasePrice; }
	public void setLastPurchasePrice(BigDecimal lastPurchasePrice) { this.lastPurchasePrice = lastPurchasePrice; }
	public BigDecimal getCurrentMarketPrice() { return currentMarketPrice; }
	public void setCurrentMarketPrice(BigDecimal currentMarketPrice) { this.currentMarketPrice = currentMarketPrice; }
	public LocalDateTime getPriceUpdatedAt() { return priceUpdatedAt; }
	public void setPriceUpdatedAt(LocalDateTime priceUpdatedAt) { this.priceUpdatedAt = priceUpdatedAt; }
	public Integer getReorderQuantity() { return reorderQuantity; }
	public void setReorderQuantity(Integer reorderQuantity) { this.reorderQuantity = reorderQuantity; }
	public Integer getPackSize() { return packSize; }
	public void setPackSize(Integer packSize) { this.packSize = packSize; }

	@Override
    public String toString() {
        return "InventoryItem{" +
            "id=" + getId() +
            ", name='" + name + '\'' +
            ", quantity=" + quantity +
            ", unitPrice=" + unitPrice +
            ", inStock=" + inStock +
            '}';
    }
}
