package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.RepairOrder;
import ru.papkov.repairlog.domain.model.StatusHistory;

import java.util.List;

/**
 * Repository для работы с историей статусов.
 *
 * @author aim-41tt
 */
@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    @Query("SELECT sh FROM StatusHistory sh " +
           "LEFT JOIN FETCH sh.status " +
           "LEFT JOIN FETCH sh.changedBy " +
           "WHERE sh.repairOrder = :order " +
           "ORDER BY sh.changedAt DESC")
    List<StatusHistory> findByRepairOrderOrderByChangedAtDesc(@Param("order") RepairOrder repairOrder);

    @Query("SELECT sh FROM StatusHistory sh " +
           "LEFT JOIN FETCH sh.status " +
           "LEFT JOIN FETCH sh.changedBy " +
           "WHERE sh.repairOrder = :order " +
           "ORDER BY sh.changedAt ASC")
    List<StatusHistory> findHistoryByOrder(@Param("order") RepairOrder order);
}
