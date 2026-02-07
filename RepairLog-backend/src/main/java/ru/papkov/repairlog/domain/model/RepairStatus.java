package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

/**
 * класс для статусов заказов на ремонт. Примеры: Новая, Принята, Диагностика, В
 * ремонте, Готов к выдаче и т.д.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "repair_statuses")

public class RepairStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Название статуса ремонта.
	 */
	@Column(name = "name", nullable = false, unique = true, length = 50)
	private String name;

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

	@Override
	public String toString() {
		return "RepairStatus{id=" + id + ", name='" + name + "'}";
	}
}
