package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

/**
 * класс для производителей устройств. Примеры: Apple, Samsung, Dell, HP и т.д.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "brands")

public class Brand {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Название бренда.
	 */
	@Column(name = "name", nullable = false, unique = true, length = 100)
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
		return "Brand{id=" + id + ", name='" + name + "'}";
	}
}
