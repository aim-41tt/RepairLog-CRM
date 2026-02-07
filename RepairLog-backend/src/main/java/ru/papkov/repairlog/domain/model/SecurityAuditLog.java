package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 *  класс для журнала аудита безопасности.
 * Ведение журнала аудита является требованием 152-ФЗ о персональных данных.
 * Логирует все действия с персональными данными и события безопасности.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "security_audit_log", indexes = {
    @Index(name = "idx_audit_employee", columnList = "employee_id"),
    @Index(name = "idx_audit_date", columnList = "created_at"),
    @Index(name = "idx_audit_event", columnList = "event_type"),
    @Index(name = "idx_audit_resource", columnList = "resource_type, resource_id")
})


public class SecurityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Тип события безопасности.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    /**
     * Сотрудник, совершивший действие.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /**
     * IP-адрес, с которого было выполнено действие.
     */
    @Column(name = "ip_address", columnDefinition = "inet")
    private String ipAddress;

    /**
     * User-Agent браузера/клиента.
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Тип ресурса (CLIENT, DEVICE, REPAIR_ORDER, EMPLOYEE и т.д.).
     */
    @Column(name = "resource_type", length = 50)
    private String resourceType;

    /**
     * ID конкретного ресурса в соответствующей таблице.
     */
    @Column(name = "resource_id")
    private Long resourceId;

    /**
     * Выполненное действие (READ, CREATE, UPDATE, DELETE и т.д.).
     */
    @Column(name = "action", length = 50)
    private String action;

    /**
     * Результат операции.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 20)
    private Result result;

    /**
     * Дополнительные детали события в формате JSON.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb")
    private Map<String, Object> details;

    /**
     * Время создания записи аудита.
     */
    @Column(name = "created_at", nullable = false)
    
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Типы событий безопасности.
     */
    public enum EventType {
        LOGIN,
        LOGOUT,
        LOGIN_FAILED,
        ACCESS_DENIED,
        DATA_ACCESS,
        DATA_CREATE,
        DATA_UPDATE,
        DATA_DELETE,
        PASSWORD_CHANGE,
        TWO_FA_ENABLED,
        TWO_FA_DISABLED,
        BACKUP_CODE_USED,
        SESSION_TERMINATED
    }

    /**
     * Результат операции.
     */
    public enum Result {
        SUCCESS,
        FAILURE,
        DENIED
    }

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
	 * @return the eventType
	 */
	public EventType getEventType() {
		return eventType;
	}

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return the employee
	 */
	public Employee getEmployee() {
		return employee;
	}

	/**
	 * @param employee the employee to set
	 */
	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * @return the resourceType
	 */
	public String getResourceType() {
		return resourceType;
	}

	/**
	 * @param resourceType the resourceType to set
	 */
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	/**
	 * @return the resourceId
	 */
	public Long getResourceId() {
		return resourceId;
	}

	/**
	 * @param resourceId the resourceId to set
	 */
	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the result
	 */
	public Result getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(Result result) {
		this.result = result;
	}

	/**
	 * @return the details
	 */
	public Map<String, Object> getDetails() {
		return details;
	}

	/**
	 * @param details the details to set
	 */
	public void setDetails(Map<String, Object> details) {
		this.details = details;
	}

	/**
	 * @return the createdAt
	 */
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt the createdAt to set
	 */
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
    public String toString() {
        return "SecurityAuditLog{" +
            "id=" + id +
            ", eventType=" + eventType +
            ", employeeId=" + (employee != null ? employee.getId() : null) +
            ", result=" + result +
            ", createdAt=" + createdAt +
            '}';
    }
}
