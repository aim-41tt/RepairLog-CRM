package ru.papkov.repairlog.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.Supplier;
import ru.papkov.repairlog.domain.model.SupplyRequest;
import ru.papkov.repairlog.domain.model.SupplyRequestStatus;
import ru.papkov.repairlog.domain.model.enums.SupplyRequestSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с заявками на поставку.
 *
 * @author aim-41tt
 */
@Repository
public interface SupplyRequestRepository extends JpaRepository<SupplyRequest, Long> {

    @EntityGraph(attributePaths = {"supplier", "status", "requestedBy", "approvedBy", "relatedRepairOrder",
                                   "items", "items.inventoryItem"})
    Optional<SupplyRequest> findById(Long id);

    @EntityGraph(attributePaths = {"supplier", "status", "requestedBy", "approvedBy", "relatedRepairOrder",
                                   "items", "items.inventoryItem"})
    List<SupplyRequest> findAll();

    @EntityGraph(attributePaths = {"supplier", "status", "requestedBy", "approvedBy", "relatedRepairOrder",
                                   "items", "items.inventoryItem"})
    Page<SupplyRequest> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"supplier", "status", "requestedBy", "approvedBy", "relatedRepairOrder",
                                   "items", "items.inventoryItem"})
    List<SupplyRequest> findBySupplier(Supplier supplier);

    @EntityGraph(attributePaths = {"supplier", "status", "requestedBy", "approvedBy", "relatedRepairOrder",
                                   "items", "items.inventoryItem"})
    List<SupplyRequest> findByStatus(SupplyRequestStatus status);

    @EntityGraph(attributePaths = {"supplier", "status", "requestedBy", "approvedBy", "relatedRepairOrder",
                                   "items", "items.inventoryItem"})
    List<SupplyRequest> findByStatusOrderByCreatedAtDesc(SupplyRequestStatus status);

    @EntityGraph(attributePaths = {"supplier", "status", "requestedBy", "approvedBy", "relatedRepairOrder",
                                   "items", "items.inventoryItem"})
    List<SupplyRequest> findBySource(SupplyRequestSource source);

    @EntityGraph(attributePaths = {"supplier", "status", "requestedBy", "approvedBy", "relatedRepairOrder",
                                   "items", "items.inventoryItem"})
    List<SupplyRequest> findBySourceAndStatus(SupplyRequestSource source, SupplyRequestStatus status);

    @EntityGraph(attributePaths = {"supplier", "status", "requestedBy", "approvedBy", "relatedRepairOrder",
                                   "items", "items.inventoryItem"})
    List<SupplyRequest> findBySupplierAndStatusAndCreatedAtAfter(
            Supplier supplier, SupplyRequestStatus status, LocalDateTime after);

    @EntityGraph(attributePaths = {"supplier", "status", "requestedBy", "approvedBy", "relatedRepairOrder",
                                   "items", "items.inventoryItem"})
    List<SupplyRequest> findByRequestedBy(Employee requestedBy);

    long countBySourceAndStatus(SupplyRequestSource source, SupplyRequestStatus status);

    long countByStatus(SupplyRequestStatus status);

    @EntityGraph(attributePaths = {"supplier", "status", "requestedBy", "approvedBy", "relatedRepairOrder",
                                   "items", "items.inventoryItem"})
    Optional<SupplyRequest> findByExternalOrderId(String externalOrderId);

    @Query("SELECT sr FROM SupplyRequest sr " +
           "LEFT JOIN FETCH sr.supplier " +
           "LEFT JOIN FETCH sr.status " +
           "LEFT JOIN FETCH sr.requestedBy " +
           "LEFT JOIN FETCH sr.approvedBy " +
           "LEFT JOIN FETCH sr.relatedRepairOrder " +
           "WHERE sr.status.name = :statusName " +
           "AND sr.expectedDeliveryDate IS NOT NULL AND sr.expectedDeliveryDate < :now")
    List<SupplyRequest> findOverdueDeliveries(
            @Param("statusName") String statusName,
            @Param("now") LocalDateTime now);

    /**
     * Атомарное получение следующего значения из PostgreSQL-последовательности.
     * Используется для генерации уникальных номеров заявок на поставку (thread-safe).
     */
    @Query(value = "SELECT nextval('supply_request_number_seq')", nativeQuery = true)
    Long getNextRequestNumber();
}
