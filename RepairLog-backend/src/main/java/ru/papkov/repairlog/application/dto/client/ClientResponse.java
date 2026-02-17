package ru.papkov.repairlog.application.dto.client;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO ответа с данными клиента.
 *
 * @author aim-41tt
 */
public class ClientResponse {
    private Long id;
    private String name;
    private String surname;
    private String patronymic;
    private String fullName;
    private LocalDate dateBirth;
    private String phone;
    private String email;
    private boolean consentGiven;
    private LocalDateTime consentDate;
    private LocalDate dataRetentionUntil;
    private LocalDateTime createdAt;

    public ClientResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public String getPatronymic() { return patronymic; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getDateBirth() { return dateBirth; }
    public void setDateBirth(LocalDate dateBirth) { this.dateBirth = dateBirth; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isConsentGiven() { return consentGiven; }
    public void setConsentGiven(boolean consentGiven) { this.consentGiven = consentGiven; }
    public LocalDateTime getConsentDate() { return consentDate; }
    public void setConsentDate(LocalDateTime consentDate) { this.consentDate = consentDate; }
    public LocalDate getDataRetentionUntil() { return dataRetentionUntil; }
    public void setDataRetentionUntil(LocalDate dataRetentionUntil) { this.dataRetentionUntil = dataRetentionUntil; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
