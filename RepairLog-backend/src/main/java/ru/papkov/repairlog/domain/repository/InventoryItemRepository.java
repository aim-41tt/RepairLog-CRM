package ru.papkov.repairlog.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @EntityGraph(attributePaths = {"degreeWear", "preferredSupplier"})
    Optional<InventoryItem> findBySerialNumber(String serialNumber);

    @EntityGraph(attributePaths = {"degreeWear", "preferredSupplier"})
    List<InventoryItem> findByInStockTrue();

    /**
     * Постраничный список активных позиций (inStock=true).
     * Используется в admin-эндпоинте для единообразной фильтрации с technician-эндпоинтом.
     */
    @EntityGraph(attributePaths = {"degreeWear", "preferredSupplier"})
    Page<InventoryItem> findByInStockTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"degreeWear", "preferredSupplier"})
    List<InventoryItem> findByIsDeviceTrue();

    @EntityGraph(attributePaths = {"degreeWear", "preferredSupplier"})
    List<InventoryItem> findByIsDeviceFalse();

    @Query("SELECT i FROM InventoryItem i " +
           "LEFT JOIN FETCH i.degreeWear " +
           "LEFT JOIN FETCH i.preferredSupplier " +
           "WHERE i.quantity < i.minStockLevel AND i.inStock = true")
    List<InventoryItem> findLowStockItems();

    @Query("SELECT i FROM InventoryItem i " +
           "LEFT JOIN FETCH i.degreeWear " +
           "LEFT JOIN FETCH i.preferredSupplier " +
           "WHERE i.quantity = 0 AND i.inStock = true")
    List<InventoryItem> findOutOfStockItems();

    @EntityGraph(attributePaths = {"degreeWear", "preferredSupplier"})
    List<InventoryItem> findByNameContainingIgnoreCase(String name);

    /**
     * Точный поиск по имени (без учёта регистра). Используется в webhook-обновлении цен
     * вместо substring-поиска, чтобы не обновлять несвязанные позиции.
     */
    List<InventoryItem> findByNameIgnoreCase(String name);

    @Query("SELECT i FROM InventoryItem i " +
           "LEFT JOIN FETCH i.degreeWear " +
           "LEFT JOIN FETCH i.preferredSupplier " +
           "WHERE i.quantity < i.minStockLevel AND i.minStockLevel > 0 AND i.isDevice = false")
    List<InventoryItem> findItemsNeedingReorder();

    @EntityGraph(attributePaths = {"degreeWear", "preferredSupplier"})
    List<InventoryItem> findByPreferredSupplier(Supplier supplier);

    @Query("SELECT i FROM InventoryItem i " +
           "LEFT JOIN FETCH i.degreeWear " +
           "LEFT JOIN FETCH i.preferredSupplier " +
           "WHERE i.preferredSupplier IS NOT NULL " +
           "AND i.preferredSupplier.integrationType <> :excludeType")
    List<InventoryItem> findByPreferredSupplierIntegrationTypeNot(@Param("excludeType") IntegrationType excludeType);

    @Query("SELECT i FROM InventoryItem i " +
           "LEFT JOIN FETCH i.degreeWear " +
           "LEFT JOIN FETCH i.preferredSupplier " +
           "WHERE i.id = :id")
    Optional<InventoryItem> findByIdWithDetails(@Param("id") Long id);
}
