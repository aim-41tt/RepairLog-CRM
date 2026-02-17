package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


/**
 *  класс для поставщиков запчастей и устройств.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
     * Название компании-поставщика.
     */
    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;

    /**
     * Телефон поставщика.
     */
    @Column(name = "phone", length = 50)
    private String phone;

    /**
     * Email поставщика.
     */
    @Column(name = "email", length = 100)
    private String email;


    /**
     * Контактное лицо поставщика.
     */
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    /**
     * Адрес поставщика.
     */
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    /**
     * ИНН поставщика.
     */
    @Column(name = "inn", length = 12)
    private String inn;

    /**
     * Активен ли поставщик.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

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


	public String getContactPerson() { return contactPerson; }
	public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
	public String getInn() { return inn; }
	public void setInn(String inn) { this.inn = inn; }
	public Boolean getActive() { return active; }
	public void setActive(Boolean active) { this.active = active; }

	@Override
    public String toString() {
        return "Supplier{" +
            "id=" + getId() +
            ", name='" + name + '\'' +
            ", phone='" + phone + '\'' +
            ", email='" + email + '\'' +
            '}';
    }
}
