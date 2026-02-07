package ru.papkov.repairlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Главный класс приложения CRM RepairLog.
 * <p>
 * Система управления сервисным центром по ремонту компьютерной и бытовой техники.
 * Реализована с использованием принципов Clean Architecture.
 * </p>
 *
 * @author aim-41tt
 * @version 1.0.0
 * @since 2026-02-06
 */
@SpringBootApplication
@EnableJpaAuditing
public class RepairLogApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(RepairLogApplication.class, args);
    }
}
