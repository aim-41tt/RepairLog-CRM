package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * класс для клиентов сервисного центра. Содержит поля для согласия на обработку
 * персональных данных (152-ФЗ).
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "clients")
public class Client extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * Имя клиента.
	 */
	@Column(name = "name", nullable = false, length = 100)
	private String name;

	/**
	 * Фамилия клиента.
	 */
	@Column(name = "surname", nullable = false, length = 100)
	private String surname;

	/**
	 * Отчество клиента.
	 */
	@Column(name = "patronymic", length = 100)
	private String patronymic;

	/**
	 * Дата рождения клиента.
	 */
	@Column(name = "date_birth", nullable = false)
	private LocalDate dateBirth;

	/**
	 * Телефон клиента (уникальный).
	 */
	@Column(name = "phone", nullable = false, unique = true, length = 20)
	private String phone;

	/**
	 * Email клиента.
	 */
	@Column(name = "email", length = 100)
	private String email;

	// ========== Поля для 152-ФЗ (согласие на обработку ПДн) ==========

	/**
	 * Флаг согласия на обработку персональных данных (152-ФЗ).
	 */
	@Column(name = "consent_given", nullable = false)

	private Boolean consentGiven = false;

	/**
	 * Дата получения согласия на обработку ПДн.
	 */
	@Column(name = "consent_date")
	private LocalDateTime consentDate;

	/**
	 * Дата до которой могут храниться персональные данные.
	 */
	@Column(name = "data_retention_until")
	private LocalDate dataRetentionUntil;

	// ========== Вспомогательные методы ==========

	/**
	 * Получить полное имя клиента в формате "Фамилия И.О."
	 */
	public String getFullName() {
		StringBuilder fullName = new StringBuilder(surname);
		fullName.append(" ").append(name.charAt(0)).append(".");
		if (patronymic != null && !patronymic.isEmpty()) {
			fullName.append(patronymic.charAt(0)).append(".");
		}
		return fullName.toString();
	}

	/**
	 * Получить полное развернутое имя в формате "Фамилия Имя Отчество"
	 */
	public String getFullNameExpanded() {
		StringBuilder fullName = new StringBuilder(surname).append(" ").append(name);
		if (patronymic != null && !patronymic.isEmpty()) {
			fullName.append(" ").append(patronymic);
		}
		return fullName.toString();
	}

	/**
	 * Предоставить согласие на обработку персональных данных.
	 */
	public void giveConsent() {
		this.consentGiven = true;
		this.consentDate = LocalDateTime.now();
	}

	/**
	 * Отозвать согласие на обработку персональных данных.
	 */
	public void revokeConsent() {
		this.consentGiven = false;
	}

	/**
	 * Анонимизировать персональные данные клиента (152-ФЗ).
	 * После вызова все ПДн заменяются на нейтральные значения.
	 * Телефон устанавливается отдельно (нужен уникальный ID).
	 */
	public void anonymize() {
		this.name = "Удалён";
		this.surname = "Удалён";
		this.patronymic = null;
		this.dateBirth = LocalDate.of(1900, 1, 1);
		this.email = null;
		this.consentGiven = false;
		this.consentDate = null;
		this.dataRetentionUntil = null;
	}

	/**
	 * Проверить, анонимизированы ли персональные данные.
	 */
	public boolean isAnonymized() {
		return "Удалён".equals(this.name) && "Удалён".equals(this.surname);
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
	 * @return the surname
	 */
	public String getSurname() {
		return surname;
	}

	/**
	 * @param surname the surname to set
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}

	/**
	 * @return the patronymic
	 */
	public String getPatronymic() {
		return patronymic;
	}

	/**
	 * @param patronymic the patronymic to set
	 */
	public void setPatronymic(String patronymic) {
		this.patronymic = patronymic;
	}

	/**
	 * @return the dateBirth
	 */
	public LocalDate getDateBirth() {
		return dateBirth;
	}

	/**
	 * @param dateBirth the dateBirth to set
	 */
	public void setDateBirth(LocalDate dateBirth) {
		this.dateBirth = dateBirth;
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the consentGiven
	 */
	public Boolean getConsentGiven() {
		return consentGiven;
	}

	/**
	 * @param consentGiven the consentGiven to set
	 */
	public void setConsentGiven(Boolean consentGiven) {
		this.consentGiven = consentGiven;
	}

	/**
	 * @return the consentDate
	 */
	public LocalDateTime getConsentDate() {
		return consentDate;
	}

	/**
	 * @param consentDate the consentDate to set
	 */
	public void setConsentDate(LocalDateTime consentDate) {
		this.consentDate = consentDate;
	}

	/**
	 * @return the dataRetentionUntil
	 */
	public LocalDate getDataRetentionUntil() {
		return dataRetentionUntil;
	}

	/**
	 * @param dataRetentionUntil the dataRetentionUntil to set
	 */
	public void setDataRetentionUntil(LocalDate dataRetentionUntil) {
		this.dataRetentionUntil = dataRetentionUntil;
	}

	@Override
	public String toString() {
		return "Client{" + "id=" + getId() + ", fullName='" + getFullName() + '\'' + ", phone='" + phone + '\''
				+ ", email='" + email + '\'' + ", consentGiven=" + consentGiven + '}';
	}
}
