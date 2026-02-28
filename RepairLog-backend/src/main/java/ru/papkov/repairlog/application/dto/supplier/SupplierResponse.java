package ru.papkov.repairlog.application.dto.supplier;

import java.time.LocalDateTime;

/**
 * DTO ответа с данными поставщика.
 *
 * @author aim-41tt
 */
public class SupplierResponse {

    private Long id;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String inn;
    private Boolean active;
    private LocalDateTime createdAt;
    private String integrationType;
    private String priceSource;
    private String orderMethod;
    private String websiteUrl;
    private String contactMessenger;
    private String priceListEmail;
    private String externalSupplierId;

    public SupplierResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getIntegrationType() { return integrationType; }
    public void setIntegrationType(String integrationType) { this.integrationType = integrationType; }
    public String getPriceSource() { return priceSource; }
    public void setPriceSource(String priceSource) { this.priceSource = priceSource; }
    public String getOrderMethod() { return orderMethod; }
    public void setOrderMethod(String orderMethod) { this.orderMethod = orderMethod; }
    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
    public String getContactMessenger() { return contactMessenger; }
    public void setContactMessenger(String contactMessenger) { this.contactMessenger = contactMessenger; }
    public String getPriceListEmail() { return priceListEmail; }
    public void setPriceListEmail(String priceListEmail) { this.priceListEmail = priceListEmail; }
    public String getExternalSupplierId() { return externalSupplierId; }
    public void setExternalSupplierId(String externalSupplierId) { this.externalSupplierId = externalSupplierId; }
}
