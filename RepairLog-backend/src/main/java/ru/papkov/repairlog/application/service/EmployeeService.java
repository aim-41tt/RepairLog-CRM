package ru.papkov.repairlog.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.employee.CreateEmployeeRequest;
import ru.papkov.repairlog.application.dto.employee.UpdateEmployeeRequest;
import ru.papkov.repairlog.application.mapper.EmployeeMapper;
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

/**
 * Сервис управления сотрудниками.
 * CRUD операции и назначение ролей. Доступен только ADMIN.
 * <p>
 * Возвращает entity — DTO-конверсия выполняется в контроллерах через {@link EmployeeMapper}.
 * </p>
 *
 * @author aim-41tt
 */
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeRepository employeeRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.employeeMapper = employeeMapper;
    }

    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден: " + id));
    }

    @Transactional(readOnly = true)
    public List<Employee> getByRole(String roleName) {
        return employeeRepository.findByRoleName(roleName);
    }

    @Transactional
    public Employee create(CreateEmployeeRequest request) {
        if (employeeRepository.existsByLogin(request.getLogin())) {
            throw new BusinessLogicException("Логин уже занят: " + request.getLogin());
        }

        Employee employee = employeeMapper.toEntity(request);
        employee.setPassword(passwordEncoder.encode(request.getPassword()));
        employee.setLastPasswordChange(LocalDateTime.now());
        employee.setRoles(resolveRoles(request.getRoles()));

        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден: " + id));

        employeeMapper.updateEntity(request, employee);
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            employee.setRoles(resolveRoles(request.getRoles()));
        }

        return employeeRepository.save(employee);
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
}
