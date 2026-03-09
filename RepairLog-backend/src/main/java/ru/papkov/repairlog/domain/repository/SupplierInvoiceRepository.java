package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.papkov.repairlog.domain.model.SupplierInvoice;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий счетов от поставщиков.
 *
 * @author aim-41tt
 */
public interface SupplierInvoiceRepository extends JpaRepository<SupplierInvoice, Long> {

    Optional<SupplierInvoice> findBySupplyRequestId(Long supplyRequestId);

    List<SupplierInvoice> findBySupplierId(Long supplierId);

    List<SupplierInvoice> findByStatus(SupplierInvoice.InvoiceStatus status);
}
