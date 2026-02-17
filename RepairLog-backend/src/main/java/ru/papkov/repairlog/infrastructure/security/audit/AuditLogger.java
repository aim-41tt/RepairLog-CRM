package ru.papkov.repairlog.infrastructure.security.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.SecurityAuditLog;
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

    public AuditLogger(SecurityAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
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
