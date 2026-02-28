package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Supplier;
import ru.papkov.repairlog.domain.model.enums.IntegrationType;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с поставщиками.
 *
 * @author aim-41tt
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByName(String name);

    List<Supplier> findByNameContainingIgnoreCase(String name);

    List<Supplier> findByIntegrationType(IntegrationType integrationType);

    List<Supplier> findByActiveTrue();

    Optional<Supplier> findByExternalSupplierId(String externalSupplierId);
}
