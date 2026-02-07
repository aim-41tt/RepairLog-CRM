package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.InventoryItem;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы со складскими запасами.
 * 
 * @author aim-41tt
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findBySerialNumber(String serialNumber);
    
    List<InventoryItem> findByInStockTrue();
    
    List<InventoryItem> findByIsDeviceTrue();
    
    List<InventoryItem> findByIsDeviceFalse();
    
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity < i.minStockLevel AND i.inStock = true")
    List<InventoryItem> findLowStockItems();
    
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity = 0 AND i.inStock = true")
    List<InventoryItem> findOutOfStockItems();
    
    List<InventoryItem> findByNameContainingIgnoreCase(String name);
}
