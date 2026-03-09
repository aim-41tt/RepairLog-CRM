package ru.papkov.repairlog.application.dto.client;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Запрос на создание нового клиента.
 *
 * @author aim-41tt
 */
public class CreateClientRequest {

    @NotBlank(message = "Имя обязательно")
    private String name;

    @NotBlank(message = "Фамилия обязательна")
    private String surname;

    private String patronymic;

    @NotNull(message = "Дата рождения обязательна")
    private LocalDate dateBirth;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^[0-9+() -]{6,20}$", message = "Некорректный формат телефона")
    private String phone;

    @Email(message = "Некорректный email")
    private String email;

    /** Согласие на обработку ПДн (152-ФЗ). */
    private boolean consentGiven;

    /** Согласие на получение уведомлений (SMS/Email). */
    private boolean notificationsEnabled;

    public CreateClientRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public String getPatronymic() { return patronymic; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }
    public LocalDate getDateBirth() { return dateBirth; }
    public void setDateBirth(LocalDate dateBirth) { this.dateBirth = dateBirth; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isConsentGiven() { return consentGiven; }
    public void setConsentGiven(boolean consentGiven) { this.consentGiven = consentGiven; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
}
