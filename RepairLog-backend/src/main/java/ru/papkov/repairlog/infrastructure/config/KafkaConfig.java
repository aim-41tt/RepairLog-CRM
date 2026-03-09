package ru.papkov.repairlog.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Конфигурация Apache Kafka для отправки уведомлений клиентам.
 * Активируется только при app.kafka.enabled=true.
 *
 * @author aim-41tt
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaConfig {

    @Value("${app.kafka.topic.notifications}")
    private String notificationsTopic;

    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(notificationsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
