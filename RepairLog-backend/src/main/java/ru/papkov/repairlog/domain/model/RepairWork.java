package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 *  класс для выполненных ремонтных работ.
 * Каждая работа привязана к чеку и может содержать список использованных запчастей.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "repair_works", indexes = {
    @Index(name = "idx_repair_works_receipt", columnList = "receipt_id"),
    @Index(name = "idx_repair_works_employee", columnList = "employee_id")
})


public class RepairWork extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
     * Чек, к которому относится работа.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    private Receipt receipt;

    /**
     * Сотрудник, выполнивший работу (TECHNICIAN).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Описание выполненной работы.
     */
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Стоимость работы (без учёта запчастей).
     */
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /**
     * Дата и время завершения работы.
     */
    @Column(name = "completed_at", nullable = false)
    
    private LocalDateTime completedAt = LocalDateTime.now();

    /**
     * Использованные запчасти в этой работе.
     */
    @OneToMany(mappedBy = "repairWork", cascade = CascadeType.ALL, orphanRemoval = true)
    
    private Set<RepairWorkItem> items = new HashSet<>();

    /**
     * Добавить запчасть к работе.
     */
    public void addItem(RepairWorkItem item) {
        items.add(item);
        item.setRepairWork(this);
    }

    /**
     * Удалить запчасть из работы.
     */
    public void removeItem(RepairWorkItem item) {
        items.remove(item);
        item.setRepairWork(null);
    }

    /**
	 * @return the receipt
	 */
	public Receipt getReceipt() {
		return receipt;
	}

	/**
	 * @param receipt the receipt to set
	 */
	public void setReceipt(Receipt receipt) {
		this.receipt = receipt;
	}

	/**
	 * @return the employee
	 */
	public Employee getEmployee() {
		return employee;
	}

	/**
	 * @param employee the employee to set
	 */
	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
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

	/**
	 * @return the completedAt
	 */
	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	/**
	 * @param completedAt the completedAt to set
	 */
	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	/**
	 * @return the items
	 */
	public Set<RepairWorkItem> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(Set<RepairWorkItem> items) {
		this.items = items;
	}

	@Override
    public String toString() {
        return "RepairWork{" +
            "id=" + getId() +
            ", receiptId=" + (receipt != null ? receipt.getId() : null) +
            ", employeeId=" + (employee != null ? employee.getId() : null) +
            ", price=" + price +
            ", completedAt=" + completedAt +
            '}';
    }
}
