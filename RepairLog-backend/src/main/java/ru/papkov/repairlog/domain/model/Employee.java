package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 *  класс для сотрудников системы.
 * Содержит поля для аутентификации, 2FA, безопасности и связи с ролями.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
     * Имя сотрудника.
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Фамилия сотрудника.
     */
    @Column(name = "surname", nullable = false, length = 100)
    private String surname;

    /**
     * Отчество сотрудника.
     */
    @Column(name = "patronymic", length = 100)
    private String patronymic;

    /**
     * Дата рождения сотрудника.
     */
    @Column(name = "date_birth", nullable = false)
    private LocalDate dateBirth;

    /**
     * Логин для входа в систему (нечувствительный к регистру).
     */
    @Column(name = "login", nullable = false, unique = true, columnDefinition = "citext")
    private String login;

    /**
     * Хеш пароля (BCrypt).
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Флаг блокировки учетной записи.
     */
    @Column(name = "blocked", nullable = false)
    
    private Boolean blocked = false;

    // ========== Поля для 2FA ==========

    /**
     * Секретный ключ для TOTP (Time-based One-Time Password).
     * Хранится в зашифрованном виде.
     */
    @Column(name = "totp_secret", length = 32)
    private String totpSecret;

    /**
     * Флаг включения двухфакторной аутентификации.
     * Обязательна для роли ADMIN.
     */
    @Column(name = "totp_enabled", nullable = false)
    
    private Boolean totpEnabled = false;

    // ========== Поля для безопасности ==========

    /**
     * Дата последней смены пароля.
     */
    @Column(name = "last_password_change", nullable = false)
    
    private LocalDateTime lastPasswordChange = LocalDateTime.now();

    /**
     * Счетчик неудачных попыток входа (защита от brute-force).
     */
    @Column(name = "failed_login_attempts", nullable = false)
    
    private Integer failedLoginAttempts = 0;

    /**
     * Время последнего успешного входа в систему.
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /**
     * Время до которого аккаунт заблокирован после превышения попыток входа.
     */
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    // ========== Связи ==========

    /**
     * Роли сотрудника (many-to-many).
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "employee_roles",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    
    private Set<Role> roles = new HashSet<>();

    /**
     * Резервные коды для 2FA.
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    
    private Set<EmployeeBackupCode> backupCodes = new HashSet<>();

    // ========== Вспомогательные методы ==========

    /**
     * Получить полное имя сотрудника в формате "Фамилия И.О."
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder(surname);
        fullName.append(" ").append(name.charAt(0)).append(".");
        if (patronymic != null && !patronymic.isEmpty()) {
            fullName.append(patronymic.charAt(0)).append(".");
        }
        return fullName.toString();
    }

    /**
     * Получить полное развернутое имя в формате "Фамилия Имя Отчество"
     */
    public String getFullNameExpanded() {
        StringBuilder fullName = new StringBuilder(surname)
            .append(" ")
            .append(name);
        if (patronymic != null && !patronymic.isEmpty()) {
            fullName.append(" ").append(patronymic);
        }
        return fullName.toString();
    }

    /**
     * Проверить, имеет ли сотрудник указанную роль.
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }

    /**
     * Проверить, заблокирован ли аккаунт.
     */
    public boolean isAccountLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Добавить роль сотруднику.
     */
    public void addRole(Role role) {
        roles.add(role);
        role.getEmployees().add(this);
    }

    /**
     * Удалить роль у сотрудника.
     */
    public void removeRole(Role role) {
        roles.remove(role);
        role.getEmployees().remove(this);
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
	 * @return the surname
	 */
	public String getSurname() {
		return surname;
	}

	/**
	 * @param surname the surname to set
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}

	/**
	 * @return the patronymic
	 */
	public String getPatronymic() {
		return patronymic;
	}

	/**
	 * @param patronymic the patronymic to set
	 */
	public void setPatronymic(String patronymic) {
		this.patronymic = patronymic;
	}

	/**
	 * @return the dateBirth
	 */
	public LocalDate getDateBirth() {
		return dateBirth;
	}

	/**
	 * @param dateBirth the dateBirth to set
	 */
	public void setDateBirth(LocalDate dateBirth) {
		this.dateBirth = dateBirth;
	}

	/**
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @param login the login to set
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the blocked
	 */
	public Boolean getBlocked() {
		return blocked;
	}

	/**
	 * @param blocked the blocked to set
	 */
	public void setBlocked(Boolean blocked) {
		this.blocked = blocked;
	}

	/**
	 * @return the totpSecret
	 */
	public String getTotpSecret() {
		return totpSecret;
	}

	/**
	 * @param totpSecret the totpSecret to set
	 */
	public void setTotpSecret(String totpSecret) {
		this.totpSecret = totpSecret;
	}

	/**
	 * @return the totpEnabled
	 */
	public Boolean getTotpEnabled() {
		return totpEnabled;
	}

	/**
	 * @param totpEnabled the totpEnabled to set
	 */
	public void setTotpEnabled(Boolean totpEnabled) {
		this.totpEnabled = totpEnabled;
	}

	/**
	 * @return the lastPasswordChange
	 */
	public LocalDateTime getLastPasswordChange() {
		return lastPasswordChange;
	}

	/**
	 * @param lastPasswordChange the lastPasswordChange to set
	 */
	public void setLastPasswordChange(LocalDateTime lastPasswordChange) {
		this.lastPasswordChange = lastPasswordChange;
	}

	/**
	 * @return the failedLoginAttempts
	 */
	public Integer getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	/**
	 * @param failedLoginAttempts the failedLoginAttempts to set
	 */
	public void setFailedLoginAttempts(Integer failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	/**
	 * @return the lastLogin
	 */
	public LocalDateTime getLastLogin() {
		return lastLogin;
	}

	/**
	 * @param lastLogin the lastLogin to set
	 */
	public void setLastLogin(LocalDateTime lastLogin) {
		this.lastLogin = lastLogin;
	}

	/**
	 * @return the accountLockedUntil
	 */
	public LocalDateTime getAccountLockedUntil() {
		return accountLockedUntil;
	}

	/**
	 * @param accountLockedUntil the accountLockedUntil to set
	 */
	public void setAccountLockedUntil(LocalDateTime accountLockedUntil) {
		this.accountLockedUntil = accountLockedUntil;
	}

	/**
	 * @return the roles
	 */
	public Set<Role> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	/**
	 * @return the backupCodes
	 */
	public Set<EmployeeBackupCode> getBackupCodes() {
		return backupCodes;
	}

	/**
	 * @param backupCodes the backupCodes to set
	 */
	public void setBackupCodes(Set<EmployeeBackupCode> backupCodes) {
		this.backupCodes = backupCodes;
	}

	@Override
    public String toString() {
        return "Employee{" +
            "id=" + getId() +
            ", login='" + login + '\'' +
            ", fullName='" + getFullName() + '\'' +
            ", blocked=" + blocked +
            '}';
    }
}
