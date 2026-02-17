# CRM RepairLog — Backend

Система управления сервисным центром по ремонту компьютерной и бытовой техники.

## Технологический стек

- **Язык:** Java 21
- **Фреймворк:** Spring Boot 3.4.3
- **База данных:** PostgreSQL 18.1
- **Кэш сессий:** Redis 7.4
- **Аутентификация:** JWT + Spring Security
- **Документация API:** SpringDoc OpenAPI (Swagger UI)
- **Маппинг:** MapStruct 1.6.3
- **Контейнеризация:** Docker Compose

## Архитектура — Clean Architecture + DDD

Проект использует принципы Clean Architecture и Domain-Driven Design 
с адаптацией под Spring Boot экосистему.
```
src/main/java/ru/papkov/repairlog/
├── domain/          # Сущности, репозитории, исключения
├── application/     # Сервисы, DTO, MapStruct-маппёры
├── infrastructure/  # Security (JWT, Redis), конфигурации
└── presentation/    # REST-контроллеры, GlobalExceptionHandler
```

## Ролевая модель

| Роль           | Описание                                    |
|---------------|----------------------------------------------|
| `ADMIN`       | Управление сотрудниками, складом, поставками |
| `TECHNICIAN`  | Диагностика, ремонт, работа со складом       |
| `RECEPTIONIST`| Приёмка устройств, клиенты, оплата           |

## Быстрый старт

### Через Docker Compose

```bash
# Скопировать и настроить переменные окружения
cp .env.example .env

# Запустить все сервисы (PostgreSQL + Redis + Backend)
docker compose up -d

# Проверить статус
docker compose ps
```

### Локальная разработка

Требуется: Java 21, Maven, PostgreSQL, Redis

```bash
# Создать БД
psql -U postgres -c "CREATE DATABASE repairlog_db;"
psql -U postgres -d repairlog_db -f repairLog-UNIFIED.sql

# Запустить Redis
redis-server

# Запустить приложение
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## API Endpoints

После запуска Swagger UI доступен по адресу:
**http://localhost:8080/swagger-ui.html**

Основные группы:

| Путь                   | Роль           | Описание                          |
|------------------------|----------------|-----------------------------------|
| `/api/auth/**`         | PUBLIC         | Вход, выход, смена пароля         |
| `/api/admin/**`        | ADMIN          | Сотрудники, склад, поставки, аудит|
| `/api/technician/**`   | TECHNICIAN     | Заказы, диагностика, ремонт       |
| `/api/receptionist/**` | RECEPTIONIST   | Клиенты, заказы, оплата           |
| `/api/reference/**`    | Authenticated  | Справочники (бренды, типы и т.д.) |

## Соответствие 152-ФЗ

Система реализует требования защиты персональных данных:
- Ролевое разграничение доступа (RBAC)
- Журнал аудита безопасности (`security_audit_log`)
- Управление согласиями на обработку ПДн
- Шифрование паролей (BCrypt)

## Автор

**@aim-41tt** — [GitHub](https://github.com/aim-41tt)
