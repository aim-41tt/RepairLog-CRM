package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.SupplierPayment;
import ru.papkov.repairlog.domain.model.SupplyRequest;

import java.util.List;

/**
 * Репозиторий оплат поставщикам.
 *
 * @author aim-41tt
 */
@Repository
public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment, Long> {

    @EntityGraph(attributePaths = {"supplyRequest", "paidBy"})
    List<SupplierPayment> findBySupplyRequest(SupplyRequest supplyRequest);

    @EntityGraph(attributePaths = {"supplyRequest", "paidBy"})
    List<SupplierPayment> findBySupplyRequestId(Long supplyRequestId);
}
