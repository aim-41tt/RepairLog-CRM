package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.SupplyRequestStatus;

import java.util.Optional;

/**
 * Repository для работы со статусами запросов на поставку.
 * 
 * @author aim-41tt
 */
@Repository
public interface SupplyRequestStatusRepository extends JpaRepository<SupplyRequestStatus, Long> {

    Optional<SupplyRequestStatus> findByName(String name);
    
    boolean existsByName(String name);
}
