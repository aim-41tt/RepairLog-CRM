package ru.papkov.repairlog.application.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос на создание нового поставщика.
 *
 * @author aim-41tt
 */
public class CreateSupplierRequest {

    @NotBlank(message = "Наименование обязательно")
    @Size(max = 200, message = "Наименование не должно превышать 200 символов")
    private String name;

    @Size(max = 100, message = "Контактное лицо не должно превышать 100 символов")
    private String contactPerson;

    @Size(max = 20, message = "Телефон не должен превышать 20 символов")
    private String phone;

    private String email;
    private String address;

    @Size(max = 12, message = "ИНН не должен превышать 12 символов")
    private String inn;

    public CreateSupplierRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getInn() { return inn; }
    public void setInn(String inn) { this.inn = inn; }
}
