package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @EntityGraph(attributePaths = {"repairOrder", "performedBy"})
    Optional<Diagnostic> findById(Long id);

    @Query("SELECT d FROM Diagnostic d " +
           "LEFT JOIN FETCH d.repairOrder " +
           "LEFT JOIN FETCH d.performedBy " +
           "WHERE d.repairOrder = :repairOrder")
    Optional<Diagnostic> findByRepairOrder(@Param("repairOrder") RepairOrder repairOrder);

    boolean existsByRepairOrder(RepairOrder repairOrder);

    /**
     * Найти диагностику по ID заказа на ремонт.
     *
     * @param repairOrderId ID заказа
     * @return диагностика если найдена
     */
    @Query("SELECT d FROM Diagnostic d " +
           "LEFT JOIN FETCH d.repairOrder " +
           "LEFT JOIN FETCH d.performedBy " +
           "WHERE d.repairOrder.id = :repairOrderId")
    Optional<Diagnostic> findByRepairOrderId(@Param("repairOrderId") Long repairOrderId);
}
