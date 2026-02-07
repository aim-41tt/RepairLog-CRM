package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * класс для истории перемещения устройств по сервисному центру. Позволяет
 * отслеживать местоположение устройства в любой момент времени.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "device_locations", indexes = { 
		@Index(name = "idx_device_locations_device", columnList = "device_id"),
		@Index(name = "idx_device_locations_date", columnList = "moved_at") 
		})
public class DeviceLocation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Устройство.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "device_id", nullable = false)
	private Device device;

	/**
	 * Местоположение устройства (Приёмка, Ремонтная зона, Склад готовой продукции и
	 * т.д.).
	 */
	@Column(name = "location", nullable = false, length = 100)
	private String location;

	/**
	 * Дата и время перемещения.
	 */
	@Column(name = "moved_at", nullable = false)

	private LocalDateTime movedAt = LocalDateTime.now();

	/**
	 * Сотрудник, переместивший устройство.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "moved_by", nullable = false)
	private Employee movedBy;

	/**
	 * Комментарий к перемещению.
	 */
	@Column(name = "comment", columnDefinition = "TEXT")
	private String comment;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
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
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the movedAt
	 */
	public LocalDateTime getMovedAt() {
		return movedAt;
	}

	/**
	 * @param movedAt the movedAt to set
	 */
	public void setMovedAt(LocalDateTime movedAt) {
		this.movedAt = movedAt;
	}

	/**
	 * @return the movedBy
	 */
	public Employee getMovedBy() {
		return movedBy;
	}

	/**
	 * @param movedBy the movedBy to set
	 */
	public void setMovedBy(Employee movedBy) {
		this.movedBy = movedBy;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "DeviceLocation{" + "id=" + id + ", deviceId=" + (device != null ? device.getId() : null)
				+ ", location='" + location + '\'' + ", movedAt=" + movedAt + '}';
	}
}
