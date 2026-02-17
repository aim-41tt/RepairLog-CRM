package ru.papkov.repairlog.application.dto.order;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Запрос на создание заказа на ремонт.
 *
 * @author aim-41tt
 */
public class CreateRepairOrderRequest {

	@NotNull(message = "ID клиента обязателен")
	private Long clientId;

	@NotNull(message = "ID устройства обязательно")
	private Long deviceId;

	private String clientComplaint; 	// жалобы клиента
	private String externalCondition; // внешнее состояние устройства
	private boolean warrantyRepair = false; // гарантийный ремонт
	private LocalDate estimatedCompletionDate;
	private Long priorityId;

	public CreateRepairOrderRequest() {
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public String getClientComplaint() {
		return clientComplaint;
	}

	public void setClientComplaint(String clientComplaint) {
		this.clientComplaint = clientComplaint;
	}

	public String getExternalCondition() {
		return externalCondition;
	}

	public void setExternalCondition(String externalCondition) {
		this.externalCondition = externalCondition;
	}

	public boolean isWarrantyRepair() {
		return warrantyRepair;
	}

	public void setWarrantyRepair(boolean warrantyRepair) {
		this.warrantyRepair = warrantyRepair;
	}

	public LocalDate getEstimatedCompletionDate() {
		return estimatedCompletionDate;
	}

	public void setEstimatedCompletionDate(LocalDate estimatedCompletionDate) {
		this.estimatedCompletionDate = estimatedCompletionDate;
	}

	public Long getPriorityId() {
		return priorityId;
	}

	public void setPriorityId(Long priorityId) {
		this.priorityId = priorityId;
	}
}
