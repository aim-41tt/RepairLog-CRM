package ru.papkov.repairlog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Smoke-тест: проверяет успешную загрузку Spring-контекста
 * с реальными PostgreSQL и Redis через Testcontainers.
 */
class RepairLogBackendApplicationTests extends AbstractIntegrationTest {

    @Test
    @DisplayName("Spring-контекст загружается успешно с Testcontainers")
    void contextLoads() {
        // Контекст загружен — PostgreSQL и Redis подключены
    }
}
