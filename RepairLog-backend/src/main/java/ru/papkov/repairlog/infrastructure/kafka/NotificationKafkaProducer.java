package ru.papkov.repairlog.infrastructure.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.papkov.repairlog.application.dto.notification.NotificationEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Продюсер Kafka для отправки уведомлений клиентам.
 * Отправляет события в топик, откуда внешний сервис уведомлений
 * считывает их и рассылает SMS/Email.
 *
 * Активируется только при app.kafka.enabled=true.
 *
 * @author aim-41tt
 */
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class NotificationKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaProducer.class);

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${app.kafka.topic.notifications}")
    private String topic;

    public NotificationKafkaProducer(KafkaTemplate<String, NotificationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Отправить событие уведомления в Kafka.
     *
     * @param event событие уведомления
     */
    public void send(NotificationEvent event) {
        String key = String.valueOf(event.getClientId());

        CompletableFuture<SendResult<String, NotificationEvent>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Ошибка отправки уведомления {} в Kafka: {}", event.getNotificationId(), ex.getMessage());
            } else {
                log.info("Уведомление {} отправлено в Kafka, partition={}, offset={}",
                        event.getNotificationId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
