package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Client;
import ru.papkov.repairlog.domain.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository для работы с уведомлениями.
 *
 * @author aim-41tt
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @EntityGraph(attributePaths = {"client", "repairOrder"})
    List<Notification> findByClient(Client client);

    @EntityGraph(attributePaths = {"client", "repairOrder"})
    List<Notification> findByClientId(Long clientId);

    @EntityGraph(attributePaths = {"client", "repairOrder"})
    List<Notification> findByRepairOrderId(Long repairOrderId);

    @EntityGraph(attributePaths = {"client", "repairOrder"})
    List<Notification> findByStatus(Notification.NotificationStatus status);

    @Query("SELECT n FROM Notification n " +
           "LEFT JOIN FETCH n.client " +
           "LEFT JOIN FETCH n.repairOrder " +
           "WHERE n.status = 'PENDING' " +
           "ORDER BY n.createdAt ASC")
    List<Notification> findPendingNotifications();

    @Query("SELECT n FROM Notification n " +
           "LEFT JOIN FETCH n.client " +
           "LEFT JOIN FETCH n.repairOrder " +
           "WHERE n.status = 'FAILED' " +
           "AND n.retryCount < :maxRetries " +
           "AND (n.lastAttempt IS NULL OR n.lastAttempt < :retryAfter)")
    List<Notification> findFailedNotificationsForRetry(@Param("maxRetries") int maxRetries,
                                                       @Param("retryAfter") LocalDateTime retryAfter);

    @EntityGraph(attributePaths = {"client", "repairOrder"})
    List<Notification> findByNotificationType(Notification.NotificationType notificationType);
}
