package ru.papkov.repairlog.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.notification.NotificationResponse;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Notification;
import ru.papkov.repairlog.domain.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис управления уведомлениями.
 * Просмотр уведомлений по клиенту и заказу.
 * Уведомления создаются автоматически триггерами БД при смене статуса заказа.
 *
 * @author aim-41tt
 */
@Service
@Transactional(readOnly = true)
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Получить уведомления по клиенту.
     *
     * @param clientId ID клиента
     * @return список уведомлений
     */
    public List<NotificationResponse> getByClient(Long clientId) {
        return notificationRepository.findByClientId(clientId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получить уведомления по заказу на ремонт.
     *
     * @param orderId ID заказа
     * @return список уведомлений
     */
    public List<NotificationResponse> getByOrder(Long orderId) {
        return notificationRepository.findByRepairOrderId(orderId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получить неотправленные уведомления (PENDING).
     *
     * @return список уведомлений со статусом PENDING
     */
    public List<NotificationResponse> getPending() {
        return notificationRepository.findByStatus(Notification.NotificationStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Пометить уведомление как отправленное.
     *
     * @param notificationId ID уведомления
     */
    @Transactional
    public void markAsSent(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Уведомление не найдено: " + notificationId));
        notification.setStatus(Notification.NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
        log.info("Уведомление {} отмечено как отправленное", notificationId);
    }

    /**
     * Обработка и отправка ожидающих уведомлений.
     * Заглушка: фактическая интеграция с SMS/Email/Push — отдельная задача.
     * Обрабатывает retry-логику (максимум 3 попытки).
     */
    @Transactional
    public void sendPending() {
        List<Notification> pending = notificationRepository.findByStatus(Notification.NotificationStatus.PENDING);
        if (pending.isEmpty()) {
            return;
        }
        log.info("Обработка {} ожидающих уведомлений", pending.size());

        for (Notification n : pending) {
            try {
                // TODO: интеграция с SMS/Email/Push провайдером
                log.info("Отправка {} уведомления клиенту id={}: {}",
                        n.getNotificationType(),
                        n.getClient() != null ? n.getClient().getId() : "N/A",
                        n.getMessage());

                n.setStatus(Notification.NotificationStatus.SENT);
                n.setSentAt(LocalDateTime.now());
                notificationRepository.save(n);
            } catch (Exception e) {
                log.error("Ошибка отправки уведомления {}: {}", n.getId(), e.getMessage());
                n.setRetryCount(n.getRetryCount() != null ? n.getRetryCount() + 1 : 1);
                n.setLastAttempt(LocalDateTime.now());
                n.setErrorMessage(e.getMessage());
                if (n.getRetryCount() >= 3) {
                    n.setStatus(Notification.NotificationStatus.FAILED);
                    log.warn("Уведомление {} помечено как FAILED после {} попыток", n.getId(), n.getRetryCount());
                }
                notificationRepository.save(n);
            }
        }
    }

    // ========== Вспомогательные методы ==========

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        r.setType(n.getNotificationType() != null ? n.getNotificationType().name() : null);
        r.setClientId(n.getClient() != null ? n.getClient().getId() : null);
        r.setClientName(n.getClient() != null
                ? n.getClient().getSurname() + " " + n.getClient().getName() : null);
        r.setRepairOrderId(n.getRepairOrder() != null ? n.getRepairOrder().getId() : null);
        r.setOrderNumber(n.getRepairOrder() != null ? n.getRepairOrder().getOrderNumber() : null);
        r.setStatus(n.getStatus() != null ? n.getStatus().name() : null);
        r.setMessageBody(n.getMessage());
        r.setSentAt(n.getSentAt());
        r.setCreatedAt(n.getCreatedAt());
        return r;
    }
}
