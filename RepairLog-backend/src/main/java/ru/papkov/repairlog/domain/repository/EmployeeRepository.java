package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Employee;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с сотрудниками.
 * 
 * @author aim-41tt
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Найти сотрудника по логину.
     *
     * @param login логин сотрудника
     * @return Optional с сотрудником
     */
    Optional<Employee> findByLogin(String login);

    /**
     * Проверить существование сотрудника по логину.
     *
     * @param login логин сотрудника
     * @return true если сотрудник существует
     */
    boolean existsByLogin(String login);

    /**
     * Найти всех активных (не заблокированных) сотрудников.
     *
     * @return список активных сотрудников
     */
    List<Employee> findAllByBlockedFalse();

    /**
     * Найти сотрудников с истекшим сроком действия пароля.
     *
     * @param date дата, до которой должен был быть изменён пароль
     * @return список сотрудников
     */
    List<Employee> findAllByLastPasswordChangeBefore(LocalDateTime date);

    /**
     * Найти сотрудников с заблокированными аккаунтами.
     *
     * @param now текущая дата и время
     * @return список заблокированных сотрудников
     */
    @Query("SELECT e FROM Employee e WHERE e.accountLockedUntil IS NOT NULL AND e.accountLockedUntil > :now")
    List<Employee> findLockedAccounts(@Param("now") LocalDateTime now);

    /**
     * Найти сотрудников по роли.
     *
     * @param roleName название роли
     * @return список сотрудников
     */
    @Query("SELECT e FROM Employee e JOIN e.roles r WHERE r.name = :roleName AND e.blocked = false")
    List<Employee> findByRoleName(@Param("roleName") String roleName);
}
