package ru.papkov.repairlog.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.SecurityAuditLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository для работы с журналом аудита безопасности.
 * 
 * @author aim-41tt
 */
@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {

    /**
     * Найти все записи аудита по сотруднику.
     *
     * @param employee сотрудник
     * @param pageable параметры пагинации
     * @return страница записей аудита
     */
    Page<SecurityAuditLog> findByEmployee(Employee employee, Pageable pageable);

    /**
     * Найти все записи аудита по ID сотрудника.
     *
     * @param employeeId ID сотрудника
     * @param pageable   параметры пагинации
     * @return страница записей аудита
     */
    Page<SecurityAuditLog> findByEmployeeId(Long employeeId, Pageable pageable);

    /**
     * Найти все записи аудита по типу события.
     *
     * @param eventType тип события
     * @param pageable параметры пагинации
     * @return страница записей аудита
     */
    Page<SecurityAuditLog> findByEventType(SecurityAuditLog.EventType eventType, Pageable pageable);

    /**
     * Найти все записи аудита за период.
     *
     * @param startDate начало периода
     * @param endDate конец периода
     * @param pageable параметры пагинации
     * @return страница записей аудита
     */
    Page<SecurityAuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Найти неудачные попытки входа для сотрудника за последние 24 часа.
     *
     * @param employee сотрудник
     * @param since время, с которого считать попытки
     * @return список записей
     */
    @Query("SELECT s FROM SecurityAuditLog s WHERE s.employee = :employee " +
           "AND s.eventType = 'LOGIN_FAILED' AND s.createdAt >= :since")
    List<SecurityAuditLog> findRecentFailedLogins(@Param("employee") Employee employee, 
                                                   @Param("since") LocalDateTime since);

    /**
     * Найти доступ к конкретному ресурсу.
     *
     * @param resourceType тип ресурса
     * @param resourceId ID ресурса
     * @param pageable параметры пагинации
     * @return страница записей
     */
    Page<SecurityAuditLog> findByResourceTypeAndResourceId(String resourceType, Long resourceId, Pageable pageable);
}
