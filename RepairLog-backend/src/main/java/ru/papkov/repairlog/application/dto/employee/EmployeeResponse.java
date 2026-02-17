package ru.papkov.repairlog.application.dto.employee;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO ответа с данными сотрудника.
 *
 * @author aim-41tt
 */
public class EmployeeResponse {

    private Long id;
    private String name;
    private String surname;
    private String patronymic;
    private String fullName;
    private LocalDate dateBirth;
    private String login;
    private boolean blocked;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private List<String> roles;

    public EmployeeResponse() {}

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
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
