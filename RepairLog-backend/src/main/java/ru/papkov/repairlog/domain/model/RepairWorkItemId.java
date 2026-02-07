package ru.papkov.repairlog.domain.model;



import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key для RepairWorkItem.
 * Состоит из repair_work_id и inventory_item_id.
 * 
 * @author aim-41tt
 */

public class RepairWorkItemId implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long repairWork;
    private Long inventoryItem;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepairWorkItemId that = (RepairWorkItemId) o;
        return Objects.equals(repairWork, that.repairWork) &&
               Objects.equals(inventoryItem, that.inventoryItem);
    }

    /**
	 * @return the repairWork
	 */
	public Long getRepairWork() {
		return repairWork;
	}

	/**
	 * @param repairWork the repairWork to set
	 */
	public void setRepairWork(Long repairWork) {
		this.repairWork = repairWork;
	}

	/**
	 * @return the inventoryItem
	 */
	public Long getInventoryItem() {
		return inventoryItem;
	}

	/**
	 * @param inventoryItem the inventoryItem to set
	 */
	public void setInventoryItem(Long inventoryItem) {
		this.inventoryItem = inventoryItem;
	}

	@Override
    public int hashCode() {
        return Objects.hash(repairWork, inventoryItem);
    }
}
