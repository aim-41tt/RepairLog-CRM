package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


/**
 *  класс для результатов диагностики устройств.
 * Один заказ может иметь только одну диагностику.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "diagnostics")
public class Diagnostic extends BaseEntity {

	private static final long serialVersionUID = 1L;

    /**
     * Заказ на ремонт (one-to-one).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_order_id", nullable = false, unique = true)
    private RepairOrder repairOrder;

    /**
     * Описание выявленной неисправности.
     */
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Предложенное решение / план ремонта.
     */
    @Column(name = "solution", columnDefinition = "TEXT")
    private String solution;

    /**
     * Сотрудник, выполнивший диагностику (TECHNICIAN).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private Employee performedBy;

    /**
	 * @return the repairOrder
	 */
	public RepairOrder getRepairOrder() {
		return repairOrder;
	}

	/**
	 * @param repairOrder the repairOrder to set
	 */
	public void setRepairOrder(RepairOrder repairOrder) {
		this.repairOrder = repairOrder;
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
	 * @return the solution
	 */
	public String getSolution() {
		return solution;
	}

	/**
	 * @param solution the solution to set
	 */
	public void setSolution(String solution) {
		this.solution = solution;
	}

	/**
	 * @return the performedBy
	 */
	public Employee getPerformedBy() {
		return performedBy;
	}

	/**
	 * @param performedBy the performedBy to set
	 */
	public void setPerformedBy(Employee performedBy) {
		this.performedBy = performedBy;
	}

	@Override
    public String toString() {
        return "Diagnostic{" +
            "id=" + getId() +
            ", orderId=" + (repairOrder != null ? repairOrder.getId() : null) +
            ", performedBy=" + (performedBy != null ? performedBy.getId() : null) +
            '}';
    }
}
