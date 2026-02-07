package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Diagnostic;
import ru.papkov.repairlog.domain.model.RepairOrder;

import java.util.Optional;

/**
 * Repository для работы с диагностикой.
 * 
 * @author aim-41tt
 */
@Repository
public interface DiagnosticRepository extends JpaRepository<Diagnostic, Long> {

    Optional<Diagnostic> findByRepairOrder(RepairOrder repairOrder);
    
    boolean existsByRepairOrder(RepairOrder repairOrder);
}
