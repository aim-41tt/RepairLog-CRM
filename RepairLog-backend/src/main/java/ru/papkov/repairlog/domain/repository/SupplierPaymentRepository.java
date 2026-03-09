package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.papkov.repairlog.domain.model.SupplierPayment;
import ru.papkov.repairlog.domain.model.SupplyRequest;

import java.util.List;

/**
 * Репозиторий оплат поставщикам.
 *
 * @author aim-41tt
 */
public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment, Long> {

    List<SupplierPayment> findBySupplyRequest(SupplyRequest supplyRequest);

    List<SupplierPayment> findBySupplyRequestId(Long supplyRequestId);
}
