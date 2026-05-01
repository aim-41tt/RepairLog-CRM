package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Receipt;
import ru.papkov.repairlog.domain.model.ReceiptPayment;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository для работы с платежами.
 *
 * @author aim-41tt
 */
@Repository
public interface ReceiptPaymentRepository extends JpaRepository<ReceiptPayment, Long> {

    @EntityGraph(attributePaths = {"acceptedBy"})
    List<ReceiptPayment> findByReceipt(Receipt receipt);

    List<ReceiptPayment> findByPaymentMethod(ReceiptPayment.PaymentMethod paymentMethod);

    @Query("SELECT COALESCE(SUM(rp.paidAmount), 0) FROM ReceiptPayment rp WHERE rp.receipt = :receipt")
    BigDecimal getTotalPaidAmount(@Param("receipt") Receipt receipt);

    /**
     * Проверяет, существует ли платёж с данным transactionId для чека.
     * Используется для идемпотентности — предотвращает дублирование платежей.
     */
    boolean existsByReceiptAndTransactionId(Receipt receipt, String transactionId);
}
