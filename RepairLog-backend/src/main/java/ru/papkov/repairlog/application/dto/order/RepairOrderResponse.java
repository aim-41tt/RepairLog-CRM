package ru.papkov.repairlog.application.dto.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO ответа с данными заказа на ремонт.
 *
 * @author aim-41tt
 */
public class RepairOrderResponse {
	private Long id;
	private String orderNumber;
	private Long clientId;
	private String clientFullName;
	private String clientPhone;
	private Long deviceId;
	private String deviceDescription;
	private String acceptedByName;
	private String assignedMasterName;
	private Long assignedMasterId;
	private String currentStatusName;
	private Long currentStatusId;
	private String priorityName;
	private String clientComplaint;
	private String externalCondition;
	private boolean warrantyRepair;
	private LocalDate estimatedCompletionDate;
	private LocalDateTime actualCompletionDate;
	private BigDecimal totalAmount;
	private String paymentStatus;
	private LocalDateTime createdAt;

	public RepairOrderResponse() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getClientFullName() {
		return clientFullName;
	}

	public void setClientFullName(String clientFullName) {
		this.clientFullName = clientFullName;
	}

	public String getClientPhone() {
		return clientPhone;
	}

	public void setClientPhone(String clientPhone) {
		this.clientPhone = clientPhone;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceDescription() {
		return deviceDescription;
	}

	public void setDeviceDescription(String deviceDescription) {
		this.deviceDescription = deviceDescription;
	}

	public String getAcceptedByName() {
		return acceptedByName;
	}

	public void setAcceptedByName(String acceptedByName) {
		this.acceptedByName = acceptedByName;
	}

	public String getAssignedMasterName() {
		return assignedMasterName;
	}

	public void setAssignedMasterName(String assignedMasterName) {
		this.assignedMasterName = assignedMasterName;
	}

	public Long getAssignedMasterId() {
		return assignedMasterId;
	}

	public void setAssignedMasterId(Long assignedMasterId) {
		this.assignedMasterId = assignedMasterId;
	}

	public String getCurrentStatusName() {
		return currentStatusName;
	}

	public void setCurrentStatusName(String currentStatusName) {
		this.currentStatusName = currentStatusName;
	}

	public Long getCurrentStatusId() {
		return currentStatusId;
	}

	public void setCurrentStatusId(Long currentStatusId) {
		this.currentStatusId = currentStatusId;
	}

	public String getPriorityName() {
		return priorityName;
	}

	public void setPriorityName(String priorityName) {
		this.priorityName = priorityName;
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

	public LocalDateTime getActualCompletionDate() {
		return actualCompletionDate;
	}

	public void setActualCompletionDate(LocalDateTime actualCompletionDate) {
		this.actualCompletionDate = actualCompletionDate;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
