package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

/**
 * класс для статусов запросов на поставку. Примеры: Новая, Подтверждена,
 * Заказана, В пути, Получена и т.д.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "supply_request_statuses")

public class SupplyRequestStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Название статуса запроса на поставку.
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
		return "SupplyRequestStatus{id=" + id + ", name='" + name + "'}";
	}
}
