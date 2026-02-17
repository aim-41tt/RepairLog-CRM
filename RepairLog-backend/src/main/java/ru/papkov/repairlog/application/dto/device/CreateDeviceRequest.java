package ru.papkov.repairlog.application.dto.device;

import jakarta.validation.constraints.NotNull;
import ru.papkov.repairlog.application.dto.common.RefField;

/**
 * Запрос на создание устройства.
 *
 * @author aim-41tt
 */
public class CreateDeviceRequest {

	@NotNull(message = "Тип устройства обязателен")
	private RefField deviceType;

	@NotNull(message = "Бренд обязателен")
	private RefField brand;

	@NotNull(message = "Модель обязательна")
	private RefField model;

	private Long clientId;
	private String serialNumber;
	private boolean clientOwned = true;

	public CreateDeviceRequest() {
	}

	public RefField getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(RefField deviceType) {
		this.deviceType = deviceType;
	}

	public RefField getBrand() {
		return brand;
	}

	public void setBrand(RefField brand) {
		this.brand = brand;
	}

	public RefField getModel() {
		return model;
	}

	public void setModel(RefField model) {
		this.model = model;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public boolean isClientOwned() {
		return clientOwned;
	}

	public void setClientOwned(boolean clientOwned) {
		this.clientOwned = clientOwned;
	}
}
