package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


import java.util.HashSet;
import java.util.Set;

/**
 *  класс для ролей пользователей в системе.
 * Поддерживаемые роли: ADMIN, TECHNICIAN, RECEPTIONIST.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название роли (ADMIN, TECHNICIAN, RECEPTIONIST).
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Сотрудники с данной ролью.
     */
    @ManyToMany(mappedBy = "roles")
    private Set<Employee> employees = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return id != null && id.equals(role.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
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
	 * @return the employees
	 */
	public Set<Employee> getEmployees() {
		return employees;
	}

	/**
	 * @param employees the employees to set
	 */
	public void setEmployees(Set<Employee> employees) {
		this.employees = employees;
	}

	@Override
    public String toString() {
        return "Role{id=" + id + ", name='" + name + "'}";
    }
}
