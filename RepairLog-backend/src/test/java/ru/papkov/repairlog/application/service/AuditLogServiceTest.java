package ru.papkov.repairlog.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.papkov.repairlog.application.dto.audit.AuditLogResponse;
import ru.papkov.repairlog.application.dto.common.PageResponse;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.SecurityAuditLog;
import ru.papkov.repairlog.domain.repository.SecurityAuditLogRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private SecurityAuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private SecurityAuditLog testLog;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("Админ");
        testEmployee.setSurname("Админов");

        testLog = new SecurityAuditLog();
        testLog.setId(1L);
        testLog.setEventType(SecurityAuditLog.EventType.LOGIN);
        testLog.setEmployee(testEmployee);
        testLog.setResult(SecurityAuditLog.Result.SUCCESS);
        testLog.setIpAddress("192.168.1.1");
        testLog.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getAll - возвращает страницу записей аудита")
    void getAll_returnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SecurityAuditLog> page = new PageImpl<>(List.of(testLog), pageable, 1);
        when(auditLogRepository.findAll(pageable)).thenReturn(page);

        PageResponse<AuditLogResponse> result = auditLogService.getAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPage()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAll - корректно маппит поля записи аудита")
    void getAll_mapsFieldsCorrectly() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SecurityAuditLog> page = new PageImpl<>(List.of(testLog), pageable, 1);
        when(auditLogRepository.findAll(pageable)).thenReturn(page);

        PageResponse<AuditLogResponse> result = auditLogService.getAll(pageable);

        AuditLogResponse response = result.getContent().get(0);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEventType()).isEqualTo("LOGIN");
        assertThat(response.getEmployeeId()).isEqualTo(1L);
        assertThat(response.getResult()).isEqualTo("SUCCESS");
        assertThat(response.getIpAddress()).isEqualTo("192.168.1.1");
    }

    @Test
    @DisplayName("getAll - пустая страница")
    void getAll_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SecurityAuditLog> page = new PageImpl<>(List.of(), pageable, 0);
        when(auditLogRepository.findAll(pageable)).thenReturn(page);

        PageResponse<AuditLogResponse> result = auditLogService.getAll(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("getByEmployee - возвращает записи по сотруднику")
    void getByEmployee_returnsPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SecurityAuditLog> page = new PageImpl<>(List.of(testLog), pageable, 1);
        when(auditLogRepository.findByEmployeeId(1L, pageable)).thenReturn(page);

        PageResponse<AuditLogResponse> result = auditLogService.getByEmployee(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmployeeId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getByPeriod - возвращает записи за период")
    void getByPeriod_returnsPaged() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 12, 31, 23, 59);
        Pageable pageable = PageRequest.of(0, 10);
        Page<SecurityAuditLog> page = new PageImpl<>(List.of(testLog), pageable, 1);

        when(auditLogRepository.findByCreatedAtBetween(from, to, pageable)).thenReturn(page);

        PageResponse<AuditLogResponse> result = auditLogService.getByPeriod(from, to, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(auditLogRepository).findByCreatedAtBetween(from, to, pageable);
    }

    @Test
    @DisplayName("getAll - корректно обрабатывает запись без сотрудника")
    void getAll_handlesNullEmployee() {
        SecurityAuditLog logWithoutEmployee = new SecurityAuditLog();
        logWithoutEmployee.setId(2L);
        logWithoutEmployee.setEventType(SecurityAuditLog.EventType.ACCESS_DENIED);
        logWithoutEmployee.setEmployee(null);
        logWithoutEmployee.setResult(SecurityAuditLog.Result.DENIED);

        Pageable pageable = PageRequest.of(0, 10);
        Page<SecurityAuditLog> page = new PageImpl<>(List.of(logWithoutEmployee), pageable, 1);
        when(auditLogRepository.findAll(pageable)).thenReturn(page);

        PageResponse<AuditLogResponse> result = auditLogService.getAll(pageable);

        AuditLogResponse response = result.getContent().get(0);
        assertThat(response.getEmployeeId()).isNull();
        assertThat(response.getEmployeeName()).isNull();
    }
}
