package ru.papkov.repairlog.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.audit.AuditLogResponse;
import ru.papkov.repairlog.application.dto.common.PageResponse;
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
     *
     * @param pageable параметры пагинации
     * @return страница записей аудита
     */
    public PageResponse<AuditLogResponse> getAll(Pageable pageable) {
        Page<SecurityAuditLog> page = auditLogRepository.findAll(pageable);
        return toPageResponse(page);
    }

    /**
     * Получить записи аудита по сотруднику.
     *
     * @param employeeId ID сотрудника
     * @param pageable   параметры пагинации
     * @return страница записей аудита
     */
    public PageResponse<AuditLogResponse> getByEmployee(Long employeeId, Pageable pageable) {
        Page<SecurityAuditLog> page = auditLogRepository.findByEmployeeId(employeeId, pageable);
        return toPageResponse(page);
    }

    /**
     * Получить записи аудита за период.
     *
     * @param from     начало периода
     * @param to       конец периода
     * @param pageable параметры пагинации
     * @return страница записей аудита
     */
    public PageResponse<AuditLogResponse> getByPeriod(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Page<SecurityAuditLog> page = auditLogRepository.findByCreatedAtBetween(from, to, pageable);
        return toPageResponse(page);
    }

    // ========== Вспомогательные методы ==========

    private PageResponse<AuditLogResponse> toPageResponse(Page<SecurityAuditLog> page) {
        PageResponse<AuditLogResponse> response = new PageResponse<>();
        response.setContent(page.getContent().stream().map(this::toResponse).toList());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        return response;
    }

    private AuditLogResponse toResponse(SecurityAuditLog log) {
        AuditLogResponse r = new AuditLogResponse();
        r.setId(log.getId());
        r.setEventType(log.getEventType() != null ? log.getEventType().name() : null);
        r.setEmployeeId(log.getEmployee() != null ? log.getEmployee().getId() : null);
        r.setEmployeeName(log.getEmployee() != null ? log.getEmployee().getFullName() : null);
        r.setIpAddress(log.getIpAddress());
        r.setResourceType(log.getResourceType());
        r.setResourceId(log.getResourceId());
        r.setAction(log.getAction());
        r.setResult(log.getResult() != null ? log.getResult().name() : null);
        r.setDetails(log.getDetails());
        r.setCreatedAt(log.getCreatedAt());
        return r;
    }
}
