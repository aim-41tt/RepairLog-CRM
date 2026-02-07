package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Supplier;
import ru.papkov.repairlog.domain.model.SupplyRequest;
import ru.papkov.repairlog.domain.model.SupplyRequestStatus;

import java.util.List;

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
}
