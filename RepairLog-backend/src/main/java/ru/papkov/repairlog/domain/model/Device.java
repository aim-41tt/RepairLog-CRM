package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


/**
 *  класс для устройств, принятых в ремонт.
 * Устройство привязано к типу, модели и клиенту.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "devices", indexes = {
    @Index(name = "idx_devices_client", columnList = "client_id"),
    @Index(name = "idx_devices_model", columnList = "model_id")
})
public class Device extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
     * Тип устройства.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_type_id", nullable = false)
    private DeviceType deviceType;

    /**
     * Модель устройства.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    /**
     * Клиент-владелец устройства.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    /**
     * Серийный номер устройства (уникальный).
     */
    @Column(name = "serial_number", unique = true, length = 100)
    private String serialNumber;

    /**
     * Принадлежит ли устройство клиенту.
     * false - для демонстрационных/тестовых устройств сервисного центра.
     */
    @Column(name = "is_client_owned", nullable = false)
    private Boolean isClientOwned = true;

    /**
     * Получить краткое описание устройства.
     */
    public String getDescription() {
        return String.format("%s %s %s",
            deviceType != null ? deviceType.getName() : "Unknown",
            model != null && model.getBrand() != null ? model.getBrand().getName() : "",
            model != null ? model.getName() : ""
        ).trim();
    }

    /**
	 * @return the deviceType
	 */
	public DeviceType getDeviceType() {
		return deviceType;
	}

	/**
	 * @param deviceType the deviceType to set
	 */
	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	/**
	 * @return the model
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(Model model) {
		this.model = model;
	}

	/**
	 * @return the client
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * @param client the client to set
	 */
	public void setClient(Client client) {
		this.client = client;
	}

	/**
	 * @return the serialNumber
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * @param serialNumber the serialNumber to set
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	/**
	 * @return the isClientOwned
	 */
	public Boolean getIsClientOwned() {
		return isClientOwned;
	}

	/**
	 * @param isClientOwned the isClientOwned to set
	 */
	public void setIsClientOwned(Boolean isClientOwned) {
		this.isClientOwned = isClientOwned;
	}

	@Override
    public String toString() {
        return "Device{" +
            "id=" + getId() +
            ", description='" + getDescription() + '\'' +
            ", serialNumber='" + serialNumber + '\'' +
            ", isClientOwned=" + isClientOwned +
            '}';
    }
}
