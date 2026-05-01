package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.SupplierInvoice;

import java.util.List;

/**
 * Репозиторий счетов от поставщиков.
 *
 * @author aim-41tt
 */
@Repository
public interface SupplierInvoiceRepository extends JpaRepository<SupplierInvoice, Long> {

    @EntityGraph(attributePaths = {"supplyRequest", "supplier"})
    List<SupplierInvoice> findBySupplyRequestId(Long supplyRequestId);

    @EntityGraph(attributePaths = {"supplyRequest", "supplier"})
    List<SupplierInvoice> findBySupplierId(Long supplierId);

    @EntityGraph(attributePaths = {"supplyRequest", "supplier"})
    List<SupplierInvoice> findByStatus(SupplierInvoice.InvoiceStatus status);
}
