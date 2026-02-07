package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.SupplyRequest;
import ru.papkov.repairlog.domain.model.SupplyRequestItem;

import java.util.List;

/**
 * Repository для работы с позициями заявок на поставку.
 * 
 * @author aim-41tt
 */
@Repository
public interface SupplyRequestItemRepository extends JpaRepository<SupplyRequestItem, Long> {

    List<SupplyRequestItem> findBySupplyRequest(SupplyRequest supplyRequest);
}
