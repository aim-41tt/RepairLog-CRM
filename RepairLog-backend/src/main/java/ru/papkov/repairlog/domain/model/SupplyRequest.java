package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;
import ru.papkov.repairlog.domain.model.enums.SupplyRequestSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Сущность заявки на поставку запчастей и компонентов.
 * Создаётся техником или администратором, когда необходимых компонентов нет на складе.
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

	@Version
	@Column(name = "version")
	private Long version;

    /**
     * Номер заявки (уникальный, генерируется автоматически).
     */
    @Column(name = "request_number", unique = true, length = 30)
    private String requestNumber;

	/**
     * Поставщик (может быть null для авто-сформированных заявок без привязки).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
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
     * Администратор, подтвердивший заявку.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;

    /**
     * Общая сумма заявки.
     */
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Комментарий к заявке.
     */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    /**
     * Ожидаемая дата доставки.
     */
    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private SupplyRequestSource source = SupplyRequestSource.MANUAL;

    @Column(name = "external_order_id", length = 100)
    private String externalOrderId;

    @Column(name = "external_order_status", length = 50)
    private String externalOrderStatus;

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

	/** @return the requestNumber */
	public String getRequestNumber() { return requestNumber; }
	/** @param requestNumber the requestNumber to set */
	public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }
	/** @return the approvedBy */
	public Employee getApprovedBy() { return approvedBy; }
	/** @param approvedBy the approvedBy to set */
	public void setApprovedBy(Employee approvedBy) { this.approvedBy = approvedBy; }
	/** @return the totalAmount */
	public BigDecimal getTotalAmount() { return totalAmount; }
	/** @param totalAmount the totalAmount to set */
	public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
	/** @return the comment */
	public String getComment() { return comment; }
	/** @param comment the comment to set */
	public void setComment(String comment) { this.comment = comment; }
	/** @return the expectedDeliveryDate */
	public LocalDateTime getExpectedDeliveryDate() { return expectedDeliveryDate; }
	/** @param expectedDeliveryDate the expectedDeliveryDate to set */
	public void setExpectedDeliveryDate(LocalDateTime expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

	public SupplyRequestSource getSource() { return source; }
	public void setSource(SupplyRequestSource source) { this.source = source; }
	public String getExternalOrderId() { return externalOrderId; }
	public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }
	public String getExternalOrderStatus() { return externalOrderStatus; }
	public void setExternalOrderStatus(String externalOrderStatus) { this.externalOrderStatus = externalOrderStatus; }

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
