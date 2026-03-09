package ru.papkov.repairlog.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.InventoryItem;
import ru.papkov.repairlog.domain.model.Supplier;
import ru.papkov.repairlog.domain.model.enums.IntegrationType;

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

    /**
     * Постраничный список активных позиций (inStock=true).
     * Используется в admin-эндпоинте для единообразной фильтрации с technician-эндпоинтом.
     */
    Page<InventoryItem> findByInStockTrue(Pageable pageable);

    List<InventoryItem> findByIsDeviceTrue();

    List<InventoryItem> findByIsDeviceFalse();

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity < i.minStockLevel AND i.inStock = true")
    List<InventoryItem> findLowStockItems();

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity = 0 AND i.inStock = true")
    List<InventoryItem> findOutOfStockItems();

    List<InventoryItem> findByNameContainingIgnoreCase(String name);

    /**
     * Точный поиск по имени (без учёта регистра). Используется в webhook-обновлении цен
     * вместо substring-поиска, чтобы не обновлять несвязанные позиции.
     */
    List<InventoryItem> findByNameIgnoreCase(String name);

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity < i.minStockLevel " +
           "AND i.minStockLevel > 0 AND i.isDevice = false")
    List<InventoryItem> findItemsNeedingReorder();

    List<InventoryItem> findByPreferredSupplier(Supplier supplier);

    @Query("SELECT i FROM InventoryItem i WHERE i.preferredSupplier IS NOT NULL " +
           "AND i.preferredSupplier.integrationType <> :excludeType")
    List<InventoryItem> findByPreferredSupplierIntegrationTypeNot(IntegrationType excludeType);
}
