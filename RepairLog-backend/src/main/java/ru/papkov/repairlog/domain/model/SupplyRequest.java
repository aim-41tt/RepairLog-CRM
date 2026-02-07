package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


import java.util.HashSet;
import java.util.Set;

/**
 *  класс для заявок на поставку запчастей и устройств.
 * Создаётся когда необходимых компонентов нет на складе.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "supply_requests", indexes = {
    @Index(name = "idx_supply_requests_status", columnList = "status_id"),
    @Index(name = "idx_supply_requests_supplier", columnList = "supplier_id")
})


public class SupplyRequest extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	/**
     * Поставщик.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    /**
     * Договор с поставщиком (опционально).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private SupplierContract contract;

    /**
     * Сотрудник, создавший заявку (TECHNICIAN или ADMIN).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private Employee requestedBy;

    /**
     * Связанный заказ на ремонт (если заявка создана для конкретного заказа).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_repair_order")
    private RepairOrder relatedRepairOrder;

    /**
     * Текущий статус заявки.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private SupplyRequestStatus status;

    /**
     * Позиции в заявке (запрашиваемые товары).
     */
    @OneToMany(mappedBy = "supplyRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    
    private Set<SupplyRequestItem> items = new HashSet<>();

    /**
     * Добавить позицию в заявку.
     */
    public void addItem(SupplyRequestItem item) {
        items.add(item);
        item.setSupplyRequest(this);
    }

    /**
     * Удалить позицию из заявки.
     */
    public void removeItem(SupplyRequestItem item) {
        items.remove(item);
        item.setSupplyRequest(null);
    }

    /**
	 * @return the supplier
	 */
	public Supplier getSupplier() {
		return supplier;
	}

	/**
	 * @param supplier the supplier to set
	 */
	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	/**
	 * @return the contract
	 */
	public SupplierContract getContract() {
		return contract;
	}

	/**
	 * @param contract the contract to set
	 */
	public void setContract(SupplierContract contract) {
		this.contract = contract;
	}

	/**
	 * @return the requestedBy
	 */
	public Employee getRequestedBy() {
		return requestedBy;
	}

	/**
	 * @param requestedBy the requestedBy to set
	 */
	public void setRequestedBy(Employee requestedBy) {
		this.requestedBy = requestedBy;
	}

	/**
	 * @return the relatedRepairOrder
	 */
	public RepairOrder getRelatedRepairOrder() {
		return relatedRepairOrder;
	}

	/**
	 * @param relatedRepairOrder the relatedRepairOrder to set
	 */
	public void setRelatedRepairOrder(RepairOrder relatedRepairOrder) {
		this.relatedRepairOrder = relatedRepairOrder;
	}

	/**
	 * @return the status
	 */
	public SupplyRequestStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(SupplyRequestStatus status) {
		this.status = status;
	}

	/**
	 * @return the items
	 */
	public Set<SupplyRequestItem> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(Set<SupplyRequestItem> items) {
		this.items = items;
	}

	@Override
    public String toString() {
        return "SupplyRequest{" +
            "id=" + getId() +
            ", supplierId=" + (supplier != null ? supplier.getId() : null) +
            ", status='" + (status != null ? status.getName() : null) + '\'' +
            ", createdAt=" + getCreatedAt() +
            '}';
    }
}
