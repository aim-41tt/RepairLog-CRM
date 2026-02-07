package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

/**
 * класс для степеней износа запчастей и устройств. Примеры: Новое (0%), Хорошее
 * (25%), Удовлетворительное (50%), Плохое (75%) и т.д.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "degree_wears")
public class DegreeWear {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Название степени износа.
	 */
	@Column(name = "name", nullable = false, unique = true, length = 50)
	private String name;

	/**
	 * Процент износа (0-100).
	 */
	@Column(name = "wear_percent")
	private Integer wearPercent;

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
	 * @return the wearPercent
	 */
	public Integer getWearPercent() {
		return wearPercent;
	}

	/**
	 * @param wearPercent the wearPercent to set
	 */
	public void setWearPercent(Integer wearPercent) {
		this.wearPercent = wearPercent;
	}

	@Override
	public String toString() {
		return "DegreeWear{id=" + id + ", name='" + name + "', wearPercent=" + wearPercent + "%}";
	}
}
