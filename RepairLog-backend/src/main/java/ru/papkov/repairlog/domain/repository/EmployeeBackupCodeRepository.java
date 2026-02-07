package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.EmployeeBackupCode;

import java.util.List;

/**
 * Repository для работы с резервными кодами 2FA.
 * 
 * @author aim-41tt
 */
@Repository
public interface EmployeeBackupCodeRepository extends JpaRepository<EmployeeBackupCode, Long> {

    /**
     * Найти все неиспользованные резервные коды сотрудника.
     *
     * @param employee сотрудник
     * @return список неиспользованных кодов
     */
    List<EmployeeBackupCode> findByEmployeeAndUsedFalse(Employee employee);

    /**
     * Удалить все резервные коды сотрудника.
     *
     * @param employee сотрудник
     */
    void deleteByEmployee(Employee employee);
}
