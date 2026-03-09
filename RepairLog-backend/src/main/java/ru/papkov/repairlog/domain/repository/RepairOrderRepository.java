package ru.papkov.repairlog.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с заказами на ремонт.
 * 
 * @author aim-41tt
 */
@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {

    Optional<RepairOrder> findByOrderNumber(String orderNumber);
    
    List<RepairOrder> findByClient(Client client);
    
    List<RepairOrder> findByDevice(Device device);
    
    List<RepairOrder> findByAssignedMaster(Employee master);
    
    List<RepairOrder> findByCurrentStatus(RepairStatus status);
    
    @Query("SELECT ro FROM RepairOrder ro " +
           "LEFT JOIN FETCH ro.client " +
           "LEFT JOIN FETCH ro.device d " +
           "LEFT JOIN FETCH d.deviceType " +
           "LEFT JOIN FETCH d.model m " +
           "LEFT JOIN FETCH m.brand " +
           "LEFT JOIN FETCH ro.acceptedBy " +
           "LEFT JOIN FETCH ro.assignedMaster " +
           "LEFT JOIN FETCH ro.currentStatus " +
           "LEFT JOIN FETCH ro.priority " +
           "WHERE ro.assignedMaster = :master " +
           "AND ro.actualCompletionDate IS NULL " +
           "ORDER BY ro.priority.sortOrder ASC, ro.createdAt ASC")
    List<RepairOrder> findActiveOrdersByMaster(@Param("master") Employee master);
    
    @Query("SELECT ro FROM RepairOrder ro " +
           "LEFT JOIN FETCH ro.client " +
           "LEFT JOIN FETCH ro.device d " +
           "LEFT JOIN FETCH d.deviceType " +
           "LEFT JOIN FETCH d.model m " +
           "LEFT JOIN FETCH m.brand " +
           "LEFT JOIN FETCH ro.acceptedBy " +
           "LEFT JOIN FETCH ro.assignedMaster " +
           "LEFT JOIN FETCH ro.currentStatus " +
           "LEFT JOIN FETCH ro.priority " +
           "WHERE ro.actualCompletionDate IS NULL " +
           "ORDER BY ro.priority.sortOrder ASC, ro.createdAt ASC")
    List<RepairOrder> findAllActiveOrders();
    
    @Query("SELECT ro FROM RepairOrder ro WHERE ro.actualCompletionDate IS NOT NULL " +
           "AND ro.actualCompletionDate BETWEEN :startDate AND :endDate")
    List<RepairOrder> findCompletedOrdersBetween(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT ro FROM RepairOrder ro " +
           "LEFT JOIN FETCH ro.client " +
           "LEFT JOIN FETCH ro.device d " +
           "LEFT JOIN FETCH d.deviceType " +
           "LEFT JOIN FETCH d.model m " +
           "LEFT JOIN FETCH m.brand " +
           "LEFT JOIN FETCH ro.acceptedBy " +
           "LEFT JOIN FETCH ro.currentStatus " +
           "LEFT JOIN FETCH ro.priority " +
           "WHERE ro.assignedMaster IS NULL " +
           "ORDER BY ro.priority.sortOrder ASC, ro.createdAt ASC")
    List<RepairOrder> findUnassignedOrders();
    
    Page<RepairOrder> findByCurrentStatus(RepairStatus status, Pageable pageable);
    
    long countByCurrentStatus(RepairStatus status);
    
    long countByAssignedMasterAndActualCompletionDateIsNull(Employee master);
}
