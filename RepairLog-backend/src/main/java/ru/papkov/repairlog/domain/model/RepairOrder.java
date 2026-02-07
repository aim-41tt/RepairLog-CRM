package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * класс для заказов на ремонт - основная бизнес-сущность системы. Связывает
 * клиента, устройство, сотрудников и отслеживает весь процесс ремонта.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "repair_orders", indexes = { @Index(name = "idx_repair_orders_client", columnList = "client_id"),
		@Index(name = "idx_repair_orders_device", columnList = "device_id"),
		@Index(name = "idx_repair_orders_status", columnList = "current_status_id"),
		@Index(name = "idx_repair_orders_master", columnList = "assigned_master_id"),
		@Index(name = "idx_repair_orders_priority", columnList = "priority_id"),
		@Index(name = "idx_repair_orders_dates", columnList = "created_at, actual_completion_date") })

public class RepairOrder extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * Уникальный номер заказа в формате RO-YYYYMMDD-NNNN. Генерируется
	 * автоматически через триггер в БД.
	 */
	@Column(name = "order_number", unique = true, length = 50)
	private String orderNumber;

	/**
	 * Клиент, который сдал устройство в ремонт.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id", nullable = false)
	private Client client;

	/**
	 * Устройство, принятое в ремонт.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "device_id", nullable = false)
	private Device device;

	/**
	 * Сотрудник, принявший заявку (RECEPTIONIST).
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "accepted_by_id", nullable = false)
	private Employee acceptedBy;

	/**
	 * Назначенный мастер (TECHNICIAN).
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_master_id")
	private Employee assignedMaster;

	/**
	 * Текущий статус заказа.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "current_status_id", nullable = false)
	private RepairStatus currentStatus;

	/**
	 * Приоритет заказа.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "priority_id")
	private RepairPriority priority;

	/**
	 * Жалоба клиента / описание проблемы.
	 */
	@Column(name = "client_complaint", columnDefinition = "TEXT")
	private String clientComplaint;

	/**
	 * Внешнее состояние устройства при приёмке.
	 */
	@Column(name = "external_condition", columnDefinition = "TEXT")
	private String externalCondition;

	/**
	 * Флаг гарантийного ремонта.
	 */
	@Column(name = "warranty_repair", nullable = false)

	private Boolean warrantyRepair = false;

	/**
	 * Предполагаемая дата завершения ремонта.
	 */
	@Column(name = "estimated_completion_date")
	private LocalDate estimatedCompletionDate;

	/**
	 * Фактическая дата завершения ремонта.
	 */
	@Column(name = "actual_completion_date")
	private LocalDateTime actualCompletionDate;

	/**
	 * Проверить, завершён ли заказ.
	 */
	public boolean isCompleted() {
		return actualCompletionDate != null;
	}

	/**
	 * Проверить, назначен ли мастер на заказ.
	 */
	public boolean hasAssignedMaster() {
		return assignedMaster != null;
	}

	/**
	 * Назначить мастера на заказ.
	 */
	public void assignMaster(Employee master) {
		this.assignedMaster = master;
	}

	/**
	 * Завершить заказ.
	 */
	public void complete() {
		this.actualCompletionDate = LocalDateTime.now();
	}

	/**
	 * @return the orderNumber
	 */
	public String getOrderNumber() {
		return orderNumber;
	}

	/**
	 * @param orderNumber the orderNumber to set
	 */
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
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
	 * @return the device
	 */
	public Device getDevice() {
		return device;
	}

	/**
	 * @param device the device to set
	 */
	public void setDevice(Device device) {
		this.device = device;
	}

	/**
	 * @return the acceptedBy
	 */
	public Employee getAcceptedBy() {
		return acceptedBy;
	}

	/**
	 * @param acceptedBy the acceptedBy to set
	 */
	public void setAcceptedBy(Employee acceptedBy) {
		this.acceptedBy = acceptedBy;
	}

	/**
	 * @return the assignedMaster
	 */
	public Employee getAssignedMaster() {
		return assignedMaster;
	}

	/**
	 * @param assignedMaster the assignedMaster to set
	 */
	public void setAssignedMaster(Employee assignedMaster) {
		this.assignedMaster = assignedMaster;
	}

	/**
	 * @return the currentStatus
	 */
	public RepairStatus getCurrentStatus() {
		return currentStatus;
	}

	/**
	 * @param currentStatus the currentStatus to set
	 */
	public void setCurrentStatus(RepairStatus currentStatus) {
		this.currentStatus = currentStatus;
	}

	/**
	 * @return the priority
	 */
	public RepairPriority getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(RepairPriority priority) {
		this.priority = priority;
	}

	/**
	 * @return the clientComplaint
	 */
	public String getClientComplaint() {
		return clientComplaint;
	}

	/**
	 * @param clientComplaint the clientComplaint to set
	 */
	public void setClientComplaint(String clientComplaint) {
		this.clientComplaint = clientComplaint;
	}

	/**
	 * @return the externalCondition
	 */
	public String getExternalCondition() {
		return externalCondition;
	}

	/**
	 * @param externalCondition the externalCondition to set
	 */
	public void setExternalCondition(String externalCondition) {
		this.externalCondition = externalCondition;
	}

	/**
	 * @return the warrantyRepair
	 */
	public Boolean getWarrantyRepair() {
		return warrantyRepair;
	}

	/**
	 * @param warrantyRepair the warrantyRepair to set
	 */
	public void setWarrantyRepair(Boolean warrantyRepair) {
		this.warrantyRepair = warrantyRepair;
	}

	/**
	 * @return the estimatedCompletionDate
	 */
	public LocalDate getEstimatedCompletionDate() {
		return estimatedCompletionDate;
	}

	/**
	 * @param estimatedCompletionDate the estimatedCompletionDate to set
	 */
	public void setEstimatedCompletionDate(LocalDate estimatedCompletionDate) {
		this.estimatedCompletionDate = estimatedCompletionDate;
	}

	/**
	 * @return the actualCompletionDate
	 */
	public LocalDateTime getActualCompletionDate() {
		return actualCompletionDate;
	}

	/**
	 * @param actualCompletionDate the actualCompletionDate to set
	 */
	public void setActualCompletionDate(LocalDateTime actualCompletionDate) {
		this.actualCompletionDate = actualCompletionDate;
	}

	@Override
	public String toString() {
		return "RepairOrder{" + "id=" + getId() + ", orderNumber='" + orderNumber + '\'' + ", clientId="
				+ (client != null ? client.getId() : null) + ", deviceId=" + (device != null ? device.getId() : null)
				+ ", currentStatus='" + (currentStatus != null ? currentStatus.getName() : null) + '\'' + ", completed="
				+ isCompleted() + '}';
	}
}
