package ru.papkov.repairlog.application.dto.supply;

import java.time.LocalDateTime;

/**
 * DTO для настройки поставок.
 */
public class SupplySettingResponse {

    private Long id;
    private String settingKey;
    private String settingValue;
    private String description;
    private LocalDateTime lastModifiedAt;
    private String modifiedByName;

    public SupplySettingResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }
    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
    public String getModifiedByName() { return modifiedByName; }
    public void setModifiedByName(String modifiedByName) { this.modifiedByName = modifiedByName; }
}
