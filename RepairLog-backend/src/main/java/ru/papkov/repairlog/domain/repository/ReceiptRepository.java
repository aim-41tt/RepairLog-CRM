package ru.papkov.repairlog.domain.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Receipt;
import ru.papkov.repairlog.domain.model.RepairOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с чеками.
 * 
 * @author aim-41tt
 */
@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Optional<Receipt> findByRepairOrder(RepairOrder repairOrder);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Receipt r WHERE r.id = :id")
    Optional<Receipt> findByIdForUpdate(@Param("id") Long id);
    
    List<Receipt> findByPaymentStatus(Receipt.PaymentStatus status);
    
    List<Receipt> findByLockedTrue();
    
    @Query("SELECT r FROM Receipt r WHERE r.createdAt BETWEEN :startDate AND :endDate " +
           "AND r.paymentStatus = 'FULLY_PAID'")
    List<Receipt> findPaidReceiptsBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
}
