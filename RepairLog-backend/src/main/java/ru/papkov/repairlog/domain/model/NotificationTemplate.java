package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

/**
 * класс для шаблонов уведомлений клиентов. Используется для автоматической
 * генерации SMS, Email и Push уведомлений.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "notification_templates")
public class NotificationTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Название шаблона.
	 */
	@Column(name = "name", nullable = false, unique = true, length = 100)
	private String name;

	/**
	 * Событие, которое триггерит отправку уведомления.
	 */
	@Column(name = "trigger_event", nullable = false, length = 50)
	private String triggerEvent;

	/**
	 * Шаблон SMS сообщения. Поддерживает переменные: {status}, {orderNumber},
	 * {clientName} и т.д.
	 */
	@Column(name = "sms_template", columnDefinition = "TEXT")
	private String smsTemplate;

	/**
	 * Тема Email письма.
	 */
	@Column(name = "email_subject", length = 200)
	private String emailSubject;

	/**
	 * Шаблон Email письма.
	 */
	@Column(name = "email_template", columnDefinition = "TEXT")
	private String emailTemplate;

	/**
	 * Флаг активности шаблона.
	 */
	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the triggerEvent
	 */
	public String getTriggerEvent() {
		return triggerEvent;
	}

	/**
	 * @param triggerEvent the triggerEvent to set
	 */
	public void setTriggerEvent(String triggerEvent) {
		this.triggerEvent = triggerEvent;
	}

	/**
	 * @return the smsTemplate
	 */
	public String getSmsTemplate() {
		return smsTemplate;
	}

	/**
	 * @param smsTemplate the smsTemplate to set
	 */
	public void setSmsTemplate(String smsTemplate) {
		this.smsTemplate = smsTemplate;
	}

	/**
	 * @return the emailSubject
	 */
	public String getEmailSubject() {
		return emailSubject;
	}

	/**
	 * @param emailSubject the emailSubject to set
	 */
	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	/**
	 * @return the emailTemplate
	 */
	public String getEmailTemplate() {
		return emailTemplate;
	}

	/**
	 * @param emailTemplate the emailTemplate to set
	 */
	public void setEmailTemplate(String emailTemplate) {
		this.emailTemplate = emailTemplate;
	}

	/**
	 * @return the isActive
	 */
	public Boolean getIsActive() {
		return isActive;
	}

	/**
	 * @param isActive the isActive to set
	 */
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	@Override
	public String toString() {
		return "NotificationTemplate{" + "id=" + id + ", name='" + name + '\'' + ", triggerEvent='" + triggerEvent
				+ '\'' + ", isActive=" + isActive + '}';
	}
}
