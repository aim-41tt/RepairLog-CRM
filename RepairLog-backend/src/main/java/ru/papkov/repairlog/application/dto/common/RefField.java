package ru.papkov.repairlog.application.dto.common;

/**
 * Универсальная ссылка на справочник: по ID или по имени. Если передан id —
 * ищет по нему. Если name — ищет или создаёт.
 */
public class RefField {

	private Long id;
	private String name;

	public RefField() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}