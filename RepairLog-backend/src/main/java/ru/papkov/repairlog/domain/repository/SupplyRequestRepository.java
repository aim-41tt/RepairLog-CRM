package ru.papkov.repairlog.domain.repository;

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

    List<SupplyRequest> findBySupplier(Supplier supplier);

    List<SupplyRequest> findByStatus(SupplyRequestStatus status);

    List<SupplyRequest> findByStatusOrderByCreatedAtDesc(SupplyRequestStatus status);

    List<SupplyRequest> findBySource(SupplyRequestSource source);

    List<SupplyRequest> findBySourceAndStatus(SupplyRequestSource source, SupplyRequestStatus status);

    List<SupplyRequest> findBySupplierAndStatusAndCreatedAtAfter(
            Supplier supplier, SupplyRequestStatus status, LocalDateTime after);

    List<SupplyRequest> findByRequestedBy(Employee requestedBy);

    long countBySourceAndStatus(SupplyRequestSource source, SupplyRequestStatus status);

    long countByStatus(SupplyRequestStatus status);

    Optional<SupplyRequest> findByExternalOrderId(String externalOrderId);

    @Query("SELECT sr FROM SupplyRequest sr WHERE sr.status.name = :statusName " +
           "AND sr.expectedDeliveryDate IS NOT NULL AND sr.expectedDeliveryDate < :now")
    List<SupplyRequest> findOverdueDeliveries(
            @Param("statusName") String statusName,
            @Param("now") LocalDateTime now);
}
