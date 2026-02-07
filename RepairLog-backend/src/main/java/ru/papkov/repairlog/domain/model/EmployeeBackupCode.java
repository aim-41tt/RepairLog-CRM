package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


import java.time.LocalDateTime;

/**
 *  класс для резервных кодов двухфакторной аутентификации.
 * Резервные коды используются для восстановления доступа при потере 2FA устройства.
 * Каждый код одноразовый.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "employee_backup_codes", indexes = {
    @Index(name = "idx_backup_codes_employee", columnList = "employee_id"),
    @Index(name = "idx_backup_codes_unused", columnList = "employee_id, used")
})


public class EmployeeBackupCode extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
     * Сотрудник, которому принадлежит резервный код.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Хеш резервного кода (BCrypt).
     * Сам код показывается пользователю только один раз при генерации.
     */
    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    /**
     * Флаг использования кода (коды одноразовые).
     */
    @Column(name = "used", nullable = false)
    
    private Boolean used = false;

    /**
     * Время использования кода.
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * Отметить код как использованный.
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
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
	 * @return the codeHash
	 */
	public String getCodeHash() {
		return codeHash;
	}

	/**
	 * @param codeHash the codeHash to set
	 */
	public void setCodeHash(String codeHash) {
		this.codeHash = codeHash;
	}

	/**
	 * @return the used
	 */
	public Boolean getUsed() {
		return used;
	}

	/**
	 * @param used the used to set
	 */
	public void setUsed(Boolean used) {
		this.used = used;
	}

	/**
	 * @return the usedAt
	 */
	public LocalDateTime getUsedAt() {
		return usedAt;
	}

	/**
	 * @param usedAt the usedAt to set
	 */
	public void setUsedAt(LocalDateTime usedAt) {
		this.usedAt = usedAt;
	}

	@Override
    public String toString() {
        return "EmployeeBackupCode{" +
            "id=" + getId() +
            ", employeeId=" + (employee != null ? employee.getId() : null) +
            ", used=" + used +
            '}';
    }
}
