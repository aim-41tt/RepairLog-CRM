````md
# RepairLog CRM

RepairLog — CRM/ERP система для сервисного центра по ремонту компьютерной и бытовой техники.  
Проект автоматизирует полный цикл работы сервисного центра: приём устройств, управление ремонтами, складом, закупками, оплатами, PDF-документами и аудитом действий сотрудников.

---

# Основные возможности

## Управление ремонтными заявками

- оформление заявки на ремонт;
- пошаговый мастер создания заказа;
- автоматическая генерация номера заявки;
- управление жизненным циклом ремонта;
- история изменения статусов;
- назначение мастеров;
- гарантийный ремонт;
- поиск и фильтрация заказов.

## Управление клиентами

- база клиентов;
- хранение контактных данных;
- согласие на обработку персональных данных;
- автоматическая анонимизация данных;
- управление уведомлениями.

## Управление устройствами

- справочники брендов, моделей и типов устройств;
- серийные номера;
- привязка устройств к клиентам;
- история перемещений устройств.

## Склад и закупки

- складской учёт;
- контроль минимальных остатков;
- автоматическое формирование заявок на закупку;
- работа с поставщиками;
- учёт поставок;
- контроль закупочных цен.

## Финансы

- квитанции;
- учёт работ;
- расчёт стоимости ремонта;
- приём оплат;
- частичные оплаты;
- контроль статусов оплаты.

## Безопасность

- JWT авторизация;
- RBAC модель ролей;
- аудит действий пользователей;
- защита персональных данных;
- блокировка учётных записей;
- хранение сессий в Redis.

## Документы

- генерация PDF документов;
- квитанции;
- документы выдачи;
- HTML → PDF рендеринг.

---

# Архитектура проекта

Проект состоит из нескольких компонентов:

```text
RepairLog CRM
│
├── RepairLog-backend     → Spring Boot REST API
├── RepairLog-frontend    → Angular SPA
├── document-api          → PDF microservice
├── PostgreSQL            → основная БД
├── Redis                 → кэш и токены
└── Kafka (optional)      → уведомления и события
````

---

# Технологический стек

## Backend

* Java 21
* Spring Boot 3.4.3
* Spring Security
* JWT (jjwt 0.12.6)
* Hibernate / JPA
* MapStruct 1.6.3
* Maven
* PostgreSQL
* Redis
* SpringDoc OpenAPI

## Frontend

* Angular 18
* TypeScript 5.5
* RxJS 7.8
* SCSS
* Angular Signals
* Standalone Components

## Infrastructure

* Docker
* Docker Compose
* Nginx
* Certbot
* Kafka (optional)

---

# Ролевая модель

| Роль           | Возможности                                     |
| -------------- | ----------------------------------------------- |
| `ADMIN`        | сотрудники, склад, поставщики, аудит, настройки |
| `RECEPTIONIST` | клиенты, приёмка устройств, заказы, оплаты      |
| `TECHNICIAN`   | диагностика, ремонт, склад, заявки на закупку   |

---

# Структура проекта

```text
RepairLog/
│
├── RepairLog-backend/
│   ├── domain/
│   ├── application/
│   ├── infrastructure/
│   └── presentation/
│
├── RepairLog-frontend/
│   ├── app/
│   ├── core/
│   ├── pages/
│   ├── shared/
│   └── assets/
│
├── document-api/
│
├── docker-compose.yml
├── .env
└── README.md
```

---

# Backend Architecture

Backend реализован по принципам:

* Clean Architecture
* Domain-Driven Design (DDD)
* Layered Architecture

## Структура backend

```text
src/main/java/ru/papkov/repairlog/
│
├── domain/          # доменная модель
├── application/     # DTO, сервисы, use-cases
├── infrastructure/  # security, config, persistence
└── presentation/    # REST controllers
```

---

# Frontend Architecture

Frontend реализован как SPA приложение на Angular 18.

## Особенности

* Standalone Components;
* Lazy Loading;
* Route Guards;
* JWT Interceptor;
* Role-based routing;
* Signals API;
* централизованные Toast/Confirm компоненты.

## Маршрутизация

```text
/login

/admin/**
/receptionist/**
/technician/**
```

---

# Статусы ремонтных заявок

| Статус             | Код              |
| ------------------ | ---------------- |
| Новая              | `NEW`            |
| Принята            | `ACCEPTED`       |
| Диагностика        | `DIAGNOSTIC`     |
| Ожидает запчастей  | `WAITING_PARTS`  |
| Ожидает клиента    | `WAITING_CLIENT` |
| В ремонте          | `IN_REPAIR`      |
| Ремонт завершён    | `REPAIR_DONE`    |
| Готов к выдаче     | `READY`          |
| Выдан              | `ISSUED`         |
| Отменён            | `CANCELLED`      |
| Гарантийный случай | `WARRANTY`       |

---

# Безопасность

## Реализовано

* JWT Access Token;
* Spring Security;
* Role-Based Access Control;
* BCrypt password hashing;
* Redis token invalidation;
* журнал аудита;
* блокировка аккаунта после неудачных попыток входа;
* срок жизни пароля;
* автоматическая анонимизация персональных данных.

## Соответствие 152-ФЗ

Система реализует:

* согласие на обработку ПДн;
* журналирование действий;
* разграничение доступа;
* хранение истории изменений;
* автоматическую анонимизацию клиентов.

---

# Docker Compose

## Запуск всего проекта

```bash
docker compose up -d
```

## Остановка

```bash
docker compose down
```

## Пересборка

```bash
docker compose up -d --build
```

---

# Переменные окружения

Пример `.env`

```env
POSTGRES_DB=repairlog_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

REDIS_PASSWORD=redis

JWT_SECRET=change_me
JWT_EXPIRATION=43200000

SPRING_PROFILES_ACTIVE=prod
```

---

# Локальный запуск Backend

## Требования

* Java 21
* Maven 3.9+
* PostgreSQL
* Redis

## Запуск

```bash
mvn spring-boot:run
```

## Swagger UI

```text
http://localhost:8080/swagger-ui.html
```

---

# Локальный запуск Frontend

## Требования

* Node.js 20+
* Angular CLI 18

## Установка зависимостей

```bash
npm install
```

## Запуск

```bash
ng serve
```

## Frontend URL

```text
http://localhost:4200
```

---

# API

## Основные группы API

| Endpoint               | Назначение                  |
| ---------------------- | --------------------------- |
| `/api/auth/**`         | авторизация                 |
| `/api/admin/**`        | административный функционал |
| `/api/receptionist/**` | работа приёмщика            |
| `/api/technician/**`   | функционал мастеров         |
| `/api/reference/**`    | справочники                 |

---

# Документы PDF

Микросервис `document-api` отвечает за:

* генерацию PDF;
* шаблоны документов;
* квитанции;
* акты выдачи;
* печатные формы.

---

# База данных

## Используется

* PostgreSQL
* SQL triggers
* Views
* Constraints
* Audit tables

## Особенности

* автоматическое создание квитанций;
* автоматическая генерация номера заказа;
* история статусов;
* журнал складских операций.

---

# Мониторинг и журналирование

## Реализовано

* аудит действий пользователей;
* логирование безопасности;
* хранение IP адресов;
* журнал операций;
* история изменений.

---

# Производительность

Система рассчитана минимум на:

* 15 одновременных пользователей;
* HikariCP pool до 20 соединений;
* контейнерное развёртывание.

---

# Планируемые улучшения

* WebSocket уведомления;
* email/SMS шлюзы;
* расширенная аналитика;
* dashboard метрики;
* multi-tenant поддержка;
* интеграции с внешними поставщиками.

---

# Автор

GitHub:
[https://github.com/aim-41tt](https://github.com/aim-41tt)

---

## Лицензия

Этот проект распространяется под **некоммерческой лицензией с запретом модификаций**.

Подробности — в файле [LICENSE](LICENSE).

```
```
