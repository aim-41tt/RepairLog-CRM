package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.InventoryItem;
import ru.papkov.repairlog.domain.model.InventoryMovement;
import ru.papkov.repairlog.domain.model.RepairOrder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository для работы с движением товаров на складе.
 * 
 * @author aim-41tt
 */
@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByInventoryItemOrderByCreatedAtDesc(InventoryItem item);
    
    List<InventoryMovement> findByRelatedRepairOrder(RepairOrder repairOrder);
    
    List<InventoryMovement> findByMovementType(InventoryMovement.MovementType movementType);
    
    @Query("SELECT im FROM InventoryMovement im WHERE im.inventoryItem = :item " +
           "AND im.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY im.createdAt DESC")
    List<InventoryMovement> findMovementsBetween(@Param("item") InventoryItem item,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
}
