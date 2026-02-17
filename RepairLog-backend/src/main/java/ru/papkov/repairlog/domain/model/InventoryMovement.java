package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

/**
 *  класс для движения товаров на складе.
 * Фиксирует все приходы, расходы, списания, резервирования и возвраты.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "inventory_movements", indexes = {
    @Index(name = "idx_inventory_movements_item", columnList = "inventory_item_id"),
    @Index(name = "idx_inventory_movements_order", columnList = "related_repair_order_id"),
    @Index(name = "idx_inventory_movements_date", columnList = "created_at")
})
public class InventoryMovement extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
     * Складской товар.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    /**
     * Тип движения.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 50)
    private MovementType movementType;

    /**
     * Количество (положительное для прихода, отрицательное для расхода).
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Связанный заказ на ремонт (если списано в ремонт).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_repair_order_id")
    private RepairOrder relatedRepairOrder;

    /**
     * Связанная заявка на поставку (если приход от поставщика).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_supply_request_id")
    private SupplyRequest relatedSupplyRequest;

    /**
     * Сотрудник, выполнивший операцию.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private Employee performedBy;

    /**
     * Комментарий к операции.
     */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    /**
     * Типы движения товаров.
     */
    public enum MovementType {
        ПРИХОД,          // Поступление от поставщика
        РАСХОД,          // Списание в ремонт
        РЕЗЕРВ,          // Резервирование под заказ
        СПИСАНИЕ,        // Списание (брак, потеря и т.д.)
        ВОЗВРАТ,         // Возврат на склад
        КОРРЕКТИРОВКА    // Инвентаризация, корректировка остатков
    }

    /** Конструктор по умолчанию (JPA). */
    public InventoryMovement() {}

    public InventoryMovement(InventoryItem inventoryItem, MovementType movementType, Integer quantity,
			RepairOrder relatedRepairOrder, SupplyRequest relatedSupplyRequest, Employee performedBy, String comment) {
		this.inventoryItem = inventoryItem;
		this.movementType = movementType;
		this.quantity = quantity;
		this.relatedRepairOrder = relatedRepairOrder;
		this.relatedSupplyRequest = relatedSupplyRequest;
		this.performedBy = performedBy;
		this.comment = comment;
	}


    public InventoryItem getInventoryItem() { return inventoryItem; }
    public void setInventoryItem(InventoryItem inventoryItem) { this.inventoryItem = inventoryItem; }
    public MovementType getMovementType() { return movementType; }
    public void setMovementType(MovementType movementType) { this.movementType = movementType; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public RepairOrder getRelatedRepairOrder() { return relatedRepairOrder; }
    public void setRelatedRepairOrder(RepairOrder relatedRepairOrder) { this.relatedRepairOrder = relatedRepairOrder; }
    public SupplyRequest getRelatedSupplyRequest() { return relatedSupplyRequest; }
    public void setRelatedSupplyRequest(SupplyRequest relatedSupplyRequest) { this.relatedSupplyRequest = relatedSupplyRequest; }
    public Employee getPerformedBy() { return performedBy; }
    public void setPerformedBy(Employee performedBy) { this.performedBy = performedBy; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

	@Override
    public String toString() {
        return "InventoryMovement{" +
            "id=" + getId() +
            ", itemId=" + (inventoryItem != null ? inventoryItem.getId() : null) +
            ", movementType=" + movementType +
            ", quantity=" + quantity +
            ", createdAt=" + getCreatedAt() +
            '}';
    }
}
