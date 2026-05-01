package ru.papkov.repairlog.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.domain.model.SecurityAuditLog;
import ru.papkov.repairlog.domain.repository.SecurityAuditLogRepository;

import java.time.LocalDateTime;

/**
 * Сервис просмотра журнала аудита безопасности (152-ФЗ).
 * Доступен только для роли ADMIN.
 *
 * @author aim-41tt
 */
@Service
@Transactional(readOnly = true)
public class AuditLogService {

    private final SecurityAuditLogRepository auditLogRepository;

    public AuditLogService(SecurityAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Получить записи аудита с пагинацией.
     */
    public Page<SecurityAuditLog> getAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Получить записи аудита по сотруднику.
     */
    public Page<SecurityAuditLog> getByEmployee(Long employeeId, Pageable pageable) {
        return auditLogRepository.findByEmployeeId(employeeId, pageable);
    }

    /**
     * Получить записи аудита за период.
     */
    public Page<SecurityAuditLog> getByPeriod(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return auditLogRepository.findByCreatedAtBetween(from, to, pageable);
    }
}
