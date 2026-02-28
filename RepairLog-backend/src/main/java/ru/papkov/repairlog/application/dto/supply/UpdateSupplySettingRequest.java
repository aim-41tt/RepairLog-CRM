package ru.papkov.repairlog.application.dto.supply;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO для обновления настройки поставок.
 */
public class UpdateSupplySettingRequest {

    @NotBlank(message = "Значение настройки обязательно")
    private String settingValue;

    public UpdateSupplySettingRequest() {}

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
}
