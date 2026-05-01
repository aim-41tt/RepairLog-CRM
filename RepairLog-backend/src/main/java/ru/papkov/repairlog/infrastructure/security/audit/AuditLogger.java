package ru.papkov.repairlog.infrastructure.security.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.SecurityAuditLog;
import ru.papkov.repairlog.domain.repository.EmployeeRepository;
import ru.papkov.repairlog.domain.repository.SecurityAuditLogRepository;

import java.util.Map;

/**
 * Сервис для записи событий аудита безопасности.
 *
 * @author aim-41tt
 */
@Service
public class AuditLogger {

    private final SecurityAuditLogRepository auditLogRepository;
    private final EmployeeRepository employeeRepository;

    public AuditLogger(SecurityAuditLogRepository auditLogRepository,
                       EmployeeRepository employeeRepository) {
        this.auditLogRepository = auditLogRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public void log(SecurityAuditLog.EventType eventType,
                    Employee employee,
                    SecurityAuditLog.Result result,
                    String ipAddress,
                    String userAgent,
                    Map<String, Object> details) {
        SecurityAuditLog log = new SecurityAuditLog();
        log.setEventType(eventType);
        log.setEmployee(employee);
        log.setResult(result);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    /**
     * Записать событие аудита по логину сотрудника.
     * Используется в контроллерах, где известен только login (из SecurityContext).
     * Реализует требования 152-ФЗ о журналировании доступа к ПДн.
     *
     * @param eventType     тип события
     * @param employeeLogin логин сотрудника
     * @param resourceType  тип ресурса (CLIENT, REPAIR_ORDER, DOCUMENT и т.д.)
     * @param resourceId    ID ресурса
     * @param action        выполненное действие (READ, CREATE, UPDATE, EXPORT и т.д.)
     * @param result        результат операции
     */
    @Transactional
    public void logEvent(SecurityAuditLog.EventType eventType,
                         String employeeLogin,
                         String resourceType,
                         Long resourceId,
                         String action,
                         SecurityAuditLog.Result result) {
        Employee emp = employeeRepository.findByLogin(employeeLogin).orElse(null);
        SecurityAuditLog log = new SecurityAuditLog();
        log.setEventType(eventType);
        log.setEmployee(emp);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setAction(action);
        log.setResult(result);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logDataAccess(Employee employee,
                              String resourceType,
                              Long resourceId,
                              String action,
                              SecurityAuditLog.Result result) {
        SecurityAuditLog log = new SecurityAuditLog();
        log.setEventType(SecurityAuditLog.EventType.DATA_ACCESS);
        log.setEmployee(employee);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setAction(action);
        log.setResult(result);
        auditLogRepository.save(log);
    }
}
