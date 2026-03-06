package ru.papkov.repairlog.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.employee.*;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.Role;
import ru.papkov.repairlog.domain.repository.EmployeeRepository;
import ru.papkov.repairlog.domain.repository.RoleRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис управления сотрудниками.
 * CRUD операции и назначение ролей. Доступен только ADMIN.
 *
 * @author aim-41tt
 */
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден: " + id));
        return toResponse(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getByRole(String roleName) {
        return employeeRepository.findByRoleName(roleName).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest request) {
        if (employeeRepository.existsByLogin(request.getLogin())) {
            throw new BusinessLogicException("Логин уже занят: " + request.getLogin());
        }

        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setSurname(request.getSurname());
        employee.setPatronymic(request.getPatronymic());
        employee.setDateBirth(request.getDateBirth());
        employee.setLogin(request.getLogin());
        employee.setPassword(passwordEncoder.encode(request.getPassword()));
        employee.setLastPasswordChange(LocalDateTime.now());

        Set<Role> roles = resolveRoles(request.getRoles());
        employee.setRoles(roles);

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    @Transactional
    public EmployeeResponse update(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден: " + id));

        if (request.getName() != null) employee.setName(request.getName());
        if (request.getSurname() != null) employee.setSurname(request.getSurname());
        if (request.getPatronymic() != null) employee.setPatronymic(request.getPatronymic());
        if (request.getDateBirth() != null) employee.setDateBirth(request.getDateBirth());
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            employee.setRoles(resolveRoles(request.getRoles()));
        }

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    /**
     * Установить пароль сотруднику (только ADMIN).
     */
    @Transactional
    public void setPassword(Long employeeId, String newPassword) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден: " + employeeId));
        employee.setPassword(passwordEncoder.encode(newPassword));
        employee.setLastPasswordChange(LocalDateTime.now());
        employeeRepository.save(employee);
    }

    /**
     * Заблокировать / разблокировать сотрудника.
     */
    @Transactional
    public void toggleBlock(Long employeeId, boolean blocked) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден: " + employeeId));
        employee.setBlocked(blocked);
        if (!blocked) {
            employee.setFailedLoginAttempts(0);
            employee.setAccountLockedUntil(null);
        }
        employeeRepository.save(employee);
    }

    // ========== Private helpers ==========

    private Set<Role> resolveRoles(List<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new EntityNotFoundException("Роль не найдена: " + roleName));
            roles.add(role);
        }
        return roles;
    }

    private EmployeeResponse toResponse(Employee e) {
        EmployeeResponse r = new EmployeeResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setSurname(e.getSurname());
        r.setPatronymic(e.getPatronymic());
        r.setFullName(e.getFullName());
        r.setDateBirth(e.getDateBirth());
        r.setLogin(e.getLogin());
        r.setBlocked(e.getBlocked());
        r.setLastLogin(e.getLastLogin());
        r.setCreatedAt(e.getCreatedAt());
        r.setRoles(e.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
        return r;
    }
}
