package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


/**
 *  класс для приоритетов заказов на ремонт.
 * Определяет очерёдность выполнения заказов.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "repair_priorities")
public class RepairPriority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название приоритета (Стандартный, Повышенный, Срочный, VIP клиент).
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Порядок сортировки (меньшее значение = выше приоритет).
     */
    @Column(name = "sort_order", nullable = false, unique = true)
    private Integer sortOrder;

    /**
     * Цвет для отображения в UI (формат #RRGGBB).
     */
    @Column(name = "color_hex", length = 7)
    private String colorHex;

    /**
     * Описание приоритета.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

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
	 * @return the sortOrder
	 */
	public Integer getSortOrder() {
		return sortOrder;
	}

	/**
	 * @param sortOrder the sortOrder to set
	 */
	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	/**
	 * @return the colorHex
	 */
	public String getColorHex() {
		return colorHex;
	}

	/**
	 * @param colorHex the colorHex to set
	 */
	public void setColorHex(String colorHex) {
		this.colorHex = colorHex;
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

	@Override
    public String toString() {
        return "RepairPriority{id=" + id + ", name='" + name + "', sortOrder=" + sortOrder + '}';
    }
}
