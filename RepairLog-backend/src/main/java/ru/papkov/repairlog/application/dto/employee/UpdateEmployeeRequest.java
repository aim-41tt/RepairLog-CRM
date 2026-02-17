package ru.papkov.repairlog.application.dto.employee;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * Запрос на обновление данных сотрудника.
 *
 * @author aim-41tt
 */
public class UpdateEmployeeRequest {

    @Size(max = 100) private String name;
    @Size(max = 100) private String surname;
    @Size(max = 100) private String patronymic;
    private LocalDate dateBirth;
    private List<String> roles;

    public UpdateEmployeeRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public String getPatronymic() { return patronymic; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }
    public LocalDate getDateBirth() { return dateBirth; }
    public void setDateBirth(LocalDate dateBirth) { this.dateBirth = dateBirth; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
