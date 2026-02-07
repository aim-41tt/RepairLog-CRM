package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

/**
 * класс для типов устройств. Примеры: Ноутбук, Настольный компьютер, Смартфон,
 * Планшет и т.д.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "device_types")

public class DeviceType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Название типа устройства.
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
		return "DeviceType{id=" + id + ", name='" + name + "'}";
	}
}
