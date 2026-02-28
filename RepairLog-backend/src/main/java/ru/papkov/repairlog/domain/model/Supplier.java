package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;
import ru.papkov.repairlog.domain.model.enums.IntegrationType;
import ru.papkov.repairlog.domain.model.enums.OrderMethod;
import ru.papkov.repairlog.domain.model.enums.PriceSource;


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

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_type", nullable = false, length = 20)
    private IntegrationType integrationType = IntegrationType.MANUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_source", nullable = false, length = 20)
    private PriceSource priceSource = PriceSource.MANUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_method", nullable = false, length = 20)
    private OrderMethod orderMethod = OrderMethod.PHONE;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "contact_messenger", length = 200)
    private String contactMessenger;

    @Column(name = "price_list_email", length = 100)
    private String priceListEmail;

    @Column(name = "external_supplier_id", length = 100)
    private String externalSupplierId;

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

	public IntegrationType getIntegrationType() { return integrationType; }
	public void setIntegrationType(IntegrationType integrationType) { this.integrationType = integrationType; }
	public PriceSource getPriceSource() { return priceSource; }
	public void setPriceSource(PriceSource priceSource) { this.priceSource = priceSource; }
	public OrderMethod getOrderMethod() { return orderMethod; }
	public void setOrderMethod(OrderMethod orderMethod) { this.orderMethod = orderMethod; }
	public String getWebsiteUrl() { return websiteUrl; }
	public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
	public String getContactMessenger() { return contactMessenger; }
	public void setContactMessenger(String contactMessenger) { this.contactMessenger = contactMessenger; }
	public String getPriceListEmail() { return priceListEmail; }
	public void setPriceListEmail(String priceListEmail) { this.priceListEmail = priceListEmail; }
	public String getExternalSupplierId() { return externalSupplierId; }
	public void setExternalSupplierId(String externalSupplierId) { this.externalSupplierId = externalSupplierId; }

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
