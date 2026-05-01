package ru.papkov.repairlog.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.notification.NotificationEvent;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Client;
import ru.papkov.repairlog.domain.model.Notification;
import ru.papkov.repairlog.domain.repository.NotificationRepository;
import ru.papkov.repairlog.infrastructure.kafka.NotificationKafkaProducer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис управления уведомлениями.
 * Просмотр уведомлений по клиенту и заказу.
 * Уведомления создаются автоматически триггерами БД при смене статуса заказа.
 * Отправка через Kafka (если включён) или заглушка.
 * <p>
 * Возвращает entity — DTO-конверсия выполняется в контроллерах через маппер.
 * </p>
 *
 * @author aim-41tt
 */
@Service
@Transactional(readOnly = true)
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private NotificationKafkaProducer kafkaProducer;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Autowired(required = false)
    public void setKafkaProducer(NotificationKafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * Получить уведомления по клиенту.
     *
     * @param clientId ID клиента
     * @return список уведомлений
     */
    public List<Notification> getByClient(Long clientId) {
        return notificationRepository.findByClientId(clientId);
    }

    /**
     * Получить уведомления по заказу на ремонт.
     *
     * @param orderId ID заказа
     * @return список уведомлений
     */
    public List<Notification> getByOrder(Long orderId) {
        return notificationRepository.findByRepairOrderId(orderId);
    }

    /**
     * Получить неотправленные уведомления (PENDING).
     *
     * @return список уведомлений со статусом PENDING
     */
    public List<Notification> getPending() {
        return notificationRepository.findByStatus(Notification.NotificationStatus.PENDING);
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
     * Периодическая обработка и отправка ожидающих уведомлений через Kafka.
     * Запускается каждые 30 секунд.
     * Если Kafka отключён (kafkaProducer == null), работает как заглушка.
     * Обрабатывает retry-логику (максимум 3 попытки).
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void sendPending() {
        List<Notification> pending = notificationRepository.findByStatus(Notification.NotificationStatus.PENDING);
        if (pending.isEmpty()) {
            return;
        }
        log.info("Обработка {} ожидающих уведомлений", pending.size());

        for (Notification n : pending) {
            try {
                if (kafkaProducer != null) {
                    NotificationEvent event = toEvent(n);
                    kafkaProducer.send(event);
                    log.info("Уведомление {} отправлено в Kafka (тип={}, клиент={})",
                            n.getId(), n.getNotificationType(),
                            n.getClient() != null ? n.getClient().getId() : "N/A");
                } else {
                    log.info("Kafka отключён. Уведомление {} (тип={}, клиент={}) отмечено как отправленное (заглушка)",
                            n.getId(), n.getNotificationType(),
                            n.getClient() != null ? n.getClient().getId() : "N/A");
                }

                n.setStatus(Notification.NotificationStatus.SENT);
                n.setSentAt(LocalDateTime.now());
                notificationRepository.save(n);
            } catch (Exception e) {
                log.error("Ошибка отправки уведомления {}: {}", n.getId(), e.getMessage());
                n.setRetryCount(n.getRetryCount() != null ? n.getRetryCount() + 1 : 1);
                n.setLastAttempt(LocalDateTime.now());
                n.setErrorMessage(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                if (n.getRetryCount() >= 3) {
                    n.setStatus(Notification.NotificationStatus.FAILED);
                    log.warn("Уведомление {} помечено как FAILED после {} попыток", n.getId(), n.getRetryCount());
                }
                notificationRepository.save(n);
            }
        }
    }

    // ========== Вспомогательные методы ==========

    private NotificationEvent toEvent(Notification n) {
        NotificationEvent event = new NotificationEvent();
        event.setNotificationId(n.getId());
        event.setNotificationType(n.getNotificationType() != null ? n.getNotificationType().name() : null);
        event.setMessage(n.getMessage());
        event.setCreatedAt(n.getCreatedAt());

        Client client = n.getClient();
        if (client != null) {
            event.setClientId(client.getId());
            event.setClientPhone(client.getPhone());
            event.setClientEmail(client.getEmail());
            event.setClientFullName(client.getFullName());
        }

        if (n.getRepairOrder() != null) {
            event.setRepairOrderId(n.getRepairOrder().getId());
            event.setOrderNumber(n.getRepairOrder().getOrderNumber());
        }

        return event;
    }
}
