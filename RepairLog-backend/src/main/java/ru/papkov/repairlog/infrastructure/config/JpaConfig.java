package ru.papkov.repairlog.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Конфигурация JPA.
 * 
 * @author aim-41tt
 */
@Configuration
@EnableJpaRepositories(basePackages = "ru.papkov.repairlog.domain.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
}
