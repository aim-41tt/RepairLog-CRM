package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.RepairStatus;

import java.util.Optional;

/**
 * Repository для работы со статусами ремонта.
 * 
 * @author aim-41tt
 */
@Repository
public interface RepairStatusRepository extends JpaRepository<RepairStatus, Long> {

    Optional<RepairStatus> findByName(String name);
    
    boolean existsByName(String name);
}
