package ru.papkov.repairlog;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Абстрактный базовый класс для всех интеграционных тестов.
 * <p>
 * Автоматически запускает Docker-контейнеры PostgreSQL и Redis через Testcontainers.
 * Контейнеры запускаются один раз (singleton) и переиспользуются всеми тестовыми классами.
 * После завершения всех тестов контейнеры автоматически останавливаются.
 * <p>
 * Используется {@code @ServiceConnection} из Spring Boot 3.4 для авто-конфигурации
 * DataSource и Redis из параметров контейнеров (без ручного @DynamicPropertySource).
 *
 * @author aim-41tt
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    /**
     * PostgreSQL контейнер с полной схемой базы данных.
     * Init-скрипт (repairLog-UNIFIED.sql) создаёт все таблицы, индексы,
     * триггеры, хранимые функции и расширение citext.
     */
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:latest")
    )
            .withDatabaseName("repairlog_test")
            .withUsername("test")
            .withPassword("test")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("repairLog-UNIFIED.sql"),
                    "/docker-entrypoint-initdb.d/init-schema.sql"
            );

    /**
     * Redis контейнер для хранения JWT токенов.
     * Spring Boot автоматически определяет Redis по имени образа
     * и настраивает spring.data.redis.host/port через @ServiceConnection.
     */
    @Container
    @ServiceConnection(name = "redis")
    static final GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:latest")
    )
            .withExposedPorts(6379);
}
