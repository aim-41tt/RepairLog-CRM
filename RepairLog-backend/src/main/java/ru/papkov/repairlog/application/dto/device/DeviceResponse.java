package ru.papkov.repairlog.application.dto.device;

import java.time.LocalDateTime;

/**
 * DTO ответа с данными устройства.
 *
 * @author aim-41tt
 */
public class DeviceResponse {
    private Long id;
    private String deviceTypeName;
    private String brandName;
    private String modelName;
    private String serialNumber;
    private boolean clientOwned;
    private Long clientId;
    private String clientFullName;
    private String description;
    private LocalDateTime createdAt;

    public DeviceResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDeviceTypeName() { return deviceTypeName; }
    public void setDeviceTypeName(String deviceTypeName) { this.deviceTypeName = deviceTypeName; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public boolean isClientOwned() { return clientOwned; }
    public void setClientOwned(boolean clientOwned) { this.clientOwned = clientOwned; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public String getClientFullName() { return clientFullName; }
    public void setClientFullName(String clientFullName) { this.clientFullName = clientFullName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
