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
