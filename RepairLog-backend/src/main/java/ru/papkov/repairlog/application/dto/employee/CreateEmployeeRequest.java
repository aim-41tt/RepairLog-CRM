package ru.papkov.repairlog.application.dto.employee;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Запрос на создание нового сотрудника.
 *
 * @author aim-41tt
 */
public class CreateEmployeeRequest {

    @NotBlank(message = "Имя обязательно")
    @Size(max = 100, message = "Имя не более 100 символов")
    private String name;

    @NotBlank(message = "Фамилия обязательна")
    @Size(max = 100, message = "Фамилия не более 100 символов")
    private String surname;

    @Size(max = 100, message = "Отчество не более 100 символов")
    private String patronymic;

    @NotNull(message = "Дата рождения обязательна")
    private LocalDate dateBirth;

    @NotBlank(message = "Логин обязателен")
    @Size(min = 3, max = 50, message = "Логин от 3 до 50 символов")
    private String login;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, max = 100, message = "Пароль от 8 до 100 символов")
    private String password;

    @NotEmpty(message = "Должна быть хотя бы одна роль")
    private List<String> roles;

    public CreateEmployeeRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public String getPatronymic() { return patronymic; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }
    public LocalDate getDateBirth() { return dateBirth; }
    public void setDateBirth(LocalDate dateBirth) { this.dateBirth = dateBirth; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
