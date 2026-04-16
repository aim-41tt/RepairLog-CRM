package ru.papkov.repairlog.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Реквизиты компании для PDF-документов.
 *
 * @author aim-41tt
 */
@Component
@ConfigurationProperties(prefix = "app.company")
public class CompanyProperties {

    private String name;
    private String address;
    private String phone;
    private String email;
    private String inn;
    private String siteUrl;
    private String messengers;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getInn() { return inn; }
    public void setInn(String inn) { this.inn = inn; }

    public String getSiteUrl() { return siteUrl; }
    public void setSiteUrl(String siteUrl) { this.siteUrl = siteUrl; }

    public String getMessengers() { return messengers; }
    public void setMessengers(String messengers) { this.messengers = messengers; }
}
