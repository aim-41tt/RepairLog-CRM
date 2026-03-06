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
	 * Название статуса ремонта (для отображения пользователю).
	 */
	@Column(name = "name", nullable = false, unique = true, length = 50)
	private String name;

	/**
	 * Машинный код статуса (NEW, ACCEPTED, ISSUED и т.д.).
	 */
	@Column(name = "code", nullable = false, unique = true, length = 30)
	private String code;

	/**
	 * Является ли статус финальным (заказ считается завершённым).
	 */
	@Column(name = "is_final", nullable = false)
	private Boolean isFinal = false;

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
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the isFinal
	 */
	public Boolean getIsFinal() {
		return isFinal;
	}

	/**
	 * @param isFinal the isFinal to set
	 */
	public void setIsFinal(Boolean isFinal) {
		this.isFinal = isFinal;
	}

	@Override
	public String toString() {
		return "RepairStatus{id=" + id + ", name='" + name + "', code='" + code + "', isFinal=" + isFinal + "'}";
	}
}
