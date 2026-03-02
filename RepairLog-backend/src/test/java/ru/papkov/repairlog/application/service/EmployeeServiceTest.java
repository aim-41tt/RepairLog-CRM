package ru.papkov.repairlog.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.papkov.repairlog.application.dto.employee.CreateEmployeeRequest;
import ru.papkov.repairlog.application.dto.employee.EmployeeResponse;
import ru.papkov.repairlog.application.dto.employee.UpdateEmployeeRequest;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.Role;
import ru.papkov.repairlog.domain.repository.EmployeeRepository;
import ru.papkov.repairlog.domain.repository.RoleRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private Role technicianRole;

    @BeforeEach
    void setUp() {
        technicianRole = new Role();
        technicianRole.setId(1L);
        technicianRole.setName("TECHNICIAN");

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("Алексей");
        testEmployee.setSurname("Иванов");
        testEmployee.setPatronymic("Петрович");
        testEmployee.setDateBirth(LocalDate.of(1985, 6, 20));
        testEmployee.setLogin("ivanov");
        testEmployee.setPassword("encodedPassword");
        testEmployee.setBlocked(false);
        testEmployee.setRoles(new HashSet<>(Set.of(technicianRole)));
    }

    @Test
    @DisplayName("getAllEmployees - возвращает список сотрудников")
    void getAllEmployees_returnsList() {
        when(employeeRepository.findAll()).thenReturn(List.of(testEmployee));

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLogin()).isEqualTo("ivanov");
        assertThat(result.get(0).getRoles()).contains("TECHNICIAN");
    }

    @Test
    @DisplayName("getById - возвращает сотрудника по ID")
    void getById_returnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        EmployeeResponse result = employeeService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Алексей");
    }

    @Test
    @DisplayName("getById - ошибка если не найден")
    void getById_throwsWhenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getByRole - возвращает сотрудников по роли")
    void getByRole_returnsList() {
        when(employeeRepository.findByRoleName("TECHNICIAN")).thenReturn(List.of(testEmployee));

        List<EmployeeResponse> result = employeeService.getByRole("TECHNICIAN");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("create - успешное создание сотрудника")
    void create_success() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setName("Новый");
        request.setSurname("Сотрудник");
        request.setPatronymic("Тестович");
        request.setDateBirth(LocalDate.of(1990, 1, 1));
        request.setLogin("new_user");
        request.setPassword("password123");
        request.setRoles(List.of("TECHNICIAN"));

        when(employeeRepository.existsByLogin("new_user")).thenReturn(false);
        when(roleRepository.findByName("TECHNICIAN")).thenReturn(Optional.of(technicianRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(2L);
            return e;
        });

        EmployeeResponse result = employeeService.create(request);

        assertThat(result.getName()).isEqualTo("Новый");
        assertThat(result.getLogin()).isEqualTo("new_user");
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("create - ошибка при дублировании логина")
    void create_throwsOnDuplicateLogin() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setLogin("ivanov");

        when(employeeRepository.existsByLogin("ivanov")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("Логин уже занят");
    }

    @Test
    @DisplayName("create - ошибка если роль не найдена")
    void create_throwsOnInvalidRole() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setName("Тест");
        request.setSurname("Тестов");
        request.setDateBirth(LocalDate.of(1990, 1, 1));
        request.setLogin("test_user");
        request.setPassword("password123");
        request.setRoles(List.of("UNKNOWN_ROLE"));

        when(employeeRepository.existsByLogin("test_user")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(roleRepository.findByName("UNKNOWN_ROLE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("UNKNOWN_ROLE");
    }

    @Test
    @DisplayName("update - успешное обновление имени")
    void update_success() {
        UpdateEmployeeRequest request = new UpdateEmployeeRequest();
        request.setName("НовоеИмя");
        request.setSurname("НоваяФамилия");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        EmployeeResponse result = employeeService.update(1L, request);

        assertThat(result).isNotNull();
        verify(employeeRepository).save(testEmployee);
    }

    @Test
    @DisplayName("update - обновление ролей")
    void update_withRoles() {
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ADMIN");

        UpdateEmployeeRequest request = new UpdateEmployeeRequest();
        request.setRoles(List.of("ADMIN"));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        employeeService.update(1L, request);

        assertThat(testEmployee.getRoles()).contains(adminRole);
    }

    @Test
    @DisplayName("setPassword - установка нового пароля")
    void setPassword_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        employeeService.setPassword(1L, "newPassword");

        assertThat(testEmployee.getPassword()).isEqualTo("encodedNewPassword");
        verify(employeeRepository).save(testEmployee);
    }

    @Test
    @DisplayName("toggleBlock - блокировка сотрудника")
    void toggleBlock_block() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        employeeService.toggleBlock(1L, true);

        assertThat(testEmployee.getBlocked()).isTrue();
    }

    @Test
    @DisplayName("toggleBlock - разблокировка сбрасывает счетчик попыток")
    void toggleBlock_unblock() {
        testEmployee.setBlocked(true);
        testEmployee.setFailedLoginAttempts(5);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        employeeService.toggleBlock(1L, false);

        assertThat(testEmployee.getBlocked()).isFalse();
        assertThat(testEmployee.getFailedLoginAttempts()).isZero();
        assertThat(testEmployee.getAccountLockedUntil()).isNull();
    }
}
