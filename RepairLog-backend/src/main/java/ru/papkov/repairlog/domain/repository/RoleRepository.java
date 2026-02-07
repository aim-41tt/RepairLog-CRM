package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Role;

import java.util.Optional;

/**
 * Repository для работы с ролями пользователей.
 * 
 * @author aim-41tt
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Найти роль по названию.
     *
     * @param name название роли
     * @return Optional с ролью
     */
    Optional<Role> findByName(String name);

    /**
     * Проверить существование роли по названию.
     *
     * @param name название роли
     * @return true если роль существует
     */
    boolean existsByName(String name);
}
