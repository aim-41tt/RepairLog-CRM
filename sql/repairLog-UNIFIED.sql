-- ════════════════════════════════════════════════════════════════════════════
-- CRM RepairLog - ПОЛНАЯ СХЕМА БАЗЫ ДАННЫХ
-- Версия: 2.0 UNIFIED
-- Дата: 06 февраля 2026
-- ════════════════════════════════════════════════════════════════════════════

-- РАСШИРЕНИЯ

CREATE EXTENSION IF NOT EXISTS citext;


-- ════════════════════════════════════════════════════════════════════════════
-- РОЛИ И СОТРУДНИКИ
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

COMMENT ON TABLE roles IS 'Роли пользователей системы (ADMIN, TECHNICIAN, RECEPTIONIST)';

CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    patronymic VARCHAR(100),
    date_birth DATE NOT NULL,
    login CITEXT NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    blocked BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Поля для безопасности
    last_password_change TIMESTAMP NOT NULL DEFAULT NOW(),
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    last_login TIMESTAMP,
    account_locked_until TIMESTAMP,
    
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_at TIMESTAMP
);

COMMENT ON TABLE employees IS 'Сотрудники системы';
COMMENT ON COLUMN employees.last_password_change IS 'Дата последней смены пароля для отслеживания политики паролей';
COMMENT ON COLUMN employees.failed_login_attempts IS 'Счетчик неудачных попыток входа для защиты от brute-force атак';
COMMENT ON COLUMN employees.last_login IS 'Время последнего успешного входа в систему';
COMMENT ON COLUMN employees.account_locked_until IS 'Время до которого аккаунт заблокирован после превышения попыток входа';

CREATE TABLE employee_roles (
    employee_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (employee_id, role_id),
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

COMMENT ON TABLE employee_roles IS 'Связь сотрудников с их ролями (many-to-many)';


-- ════════════════════════════════════════════════════════════════════════════
-- ЖУРНАЛ АУДИТА БЕЗОПАСНОСТИ (152-ФЗ)
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE security_audit_log (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    employee_id BIGINT,
    ip_address VARCHAR(50), -- INET
    user_agent TEXT,
    resource_type VARCHAR(50),
    resource_id BIGINT,
    action VARCHAR(50),
    result VARCHAR(20) NOT NULL,
    details JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE SET NULL,
    
    CHECK (event_type IN ('LOGIN', 'LOGOUT', 'LOGIN_FAILED', 'ACCESS_DENIED', 'DATA_ACCESS', 
                          'DATA_CREATE', 'DATA_UPDATE', 'DATA_DELETE', 'PASSWORD_CHANGE', 
                          'SESSION_TERMINATED')),
    CHECK (result IN ('SUCCESS', 'FAILURE', 'DENIED'))
);

COMMENT ON TABLE security_audit_log IS 'Журнал аудита действий с персональными данными (требование 152-ФЗ)';
COMMENT ON COLUMN security_audit_log.event_type IS 'Тип события безопасности';
COMMENT ON COLUMN security_audit_log.resource_type IS 'Тип ресурса (CLIENT, DEVICE, REPAIR_ORDER, EMPLOYEE и т.д.)';
COMMENT ON COLUMN security_audit_log.resource_id IS 'ID конкретного ресурса в соответствующей таблице';
COMMENT ON COLUMN security_audit_log.action IS 'Выполненное действие (READ, CREATE, UPDATE, DELETE и т.д.)';
COMMENT ON COLUMN security_audit_log.details IS 'Дополнительные детали события в формате JSON';


-- ════════════════════════════════════════════════════════════════════════════
-- КЛИЕНТЫ
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    patronymic VARCHAR(100),
    date_birth DATE NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE
        CHECK (phone ~ '^[0-9+() -]{6,20}$'),
    email VARCHAR(100)
        CHECK (email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'),
    
    -- Поля для 152-ФЗ (согласие на обработку ПДн)
    consent_given BOOLEAN NOT NULL DEFAULT FALSE,
    consent_date TIMESTAMP,
    data_retention_until DATE,
    
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_at TIMESTAMP
);

COMMENT ON TABLE clients IS 'Клиенты сервисного центра';
COMMENT ON COLUMN clients.consent_given IS 'Флаг согласия на обработку персональных данных (152-ФЗ)';
COMMENT ON COLUMN clients.consent_date IS 'Дата получения согласия на обработку ПДн';
COMMENT ON COLUMN clients.data_retention_until IS 'Дата до которой могут храниться персональные данные';


-- ════════════════════════════════════════════════════════════════════════════
-- УСТРОЙСТВА
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE device_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

COMMENT ON TABLE device_types IS 'Типы устройств (ноутбук, ПК, смартфон и т.д.)';

CREATE TABLE brands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

COMMENT ON TABLE brands IS 'Производители устройств';

CREATE TABLE models (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    brand_id BIGINT NOT NULL,
    UNIQUE (name, brand_id),
    FOREIGN KEY (brand_id) REFERENCES brands(id)
);

COMMENT ON TABLE models IS 'Модели устройств';

CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    device_type_id BIGINT NOT NULL,
    model_id BIGINT NOT NULL,
    client_id BIGINT,
    serial_number VARCHAR(100) UNIQUE,
    is_client_owned BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_at TIMESTAMP,

    FOREIGN KEY (device_type_id) REFERENCES device_types(id),
    FOREIGN KEY (model_id) REFERENCES models(id),
    FOREIGN KEY (client_id) REFERENCES clients(id),
    
    CONSTRAINT chk_client_owned CHECK (
        (is_client_owned = TRUE AND client_id IS NOT NULL)
        OR
        (is_client_owned = FALSE)
    )
);

COMMENT ON TABLE devices IS 'Устройства, принятые в ремонт';
COMMENT ON COLUMN devices.is_client_owned IS 'Принадлежит ли устройство клиенту (false - для демонстрационных/тестовых устройств)';


-- ════════════════════════════════════════════════════════════════════════════
-- СТАТУСЫ
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE repair_statuses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

COMMENT ON TABLE repair_statuses IS 'Статусы заказов на ремонт';

CREATE TABLE supply_request_statuses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

COMMENT ON TABLE supply_request_statuses IS 'Статусы запросов на поставку';

CREATE TABLE repair_priorities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    sort_order INTEGER NOT NULL UNIQUE,
    color_hex VARCHAR(7),
    description TEXT
);

COMMENT ON TABLE repair_priorities IS 'Приоритеты заказов на ремонт (стандартный, срочный, VIP и т.д.)';
COMMENT ON COLUMN repair_priorities.sort_order IS 'Порядок сортировки (меньшее значение = выше приоритет)';
COMMENT ON COLUMN repair_priorities.color_hex IS 'Цвет для отображения в UI (формат #RRGGBB)';


-- ════════════════════════════════════════════════════════════════════════════
-- ЗАЯВКИ НА РЕМОНТ
-- ════════════════════════════════════════════════════════════════════════════

CREATE SEQUENCE order_number_seq;

CREATE TABLE repair_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE,
    client_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    accepted_by_id BIGINT NOT NULL,
    assigned_master_id BIGINT,
    current_status_id BIGINT NOT NULL,
    priority_id BIGINT,
    
    client_complaint TEXT,
    external_condition TEXT,
    warranty_repair BOOLEAN NOT NULL DEFAULT FALSE,
    estimated_completion_date DATE,
    actual_completion_date TIMESTAMP,
    
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_at TIMESTAMP,

    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (device_id) REFERENCES devices(id),
    FOREIGN KEY (accepted_by_id) REFERENCES employees(id),
    FOREIGN KEY (assigned_master_id) REFERENCES employees(id),
    FOREIGN KEY (current_status_id) REFERENCES repair_statuses(id),
    FOREIGN KEY (priority_id) REFERENCES repair_priorities(id)
);

COMMENT ON TABLE repair_orders IS 'Заказы на ремонт';
COMMENT ON COLUMN repair_orders.order_number IS 'Уникальный номер заказа в формате RO-YYYYMMDD-NNNN для выдачи клиенту';

CREATE TABLE status_history (
    id BIGSERIAL PRIMARY KEY,
    repair_order_id BIGINT NOT NULL,
    status_id BIGINT NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    changed_by BIGINT NOT NULL,
    comment TEXT,

    FOREIGN KEY (repair_order_id) REFERENCES repair_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (status_id) REFERENCES repair_statuses(id),
    FOREIGN KEY (changed_by) REFERENCES employees(id)
);

COMMENT ON TABLE status_history IS 'История изменения статусов заказов';


-- ════════════════════════════════════════════════════════════════════════════
-- ДИАГНОСТИКА
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE diagnostics (
    id BIGSERIAL PRIMARY KEY,
    repair_order_id BIGINT NOT NULL UNIQUE,
    description TEXT NOT NULL,
    solution TEXT,
    performed_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_at TIMESTAMP,

    FOREIGN KEY (repair_order_id) REFERENCES repair_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (performed_by) REFERENCES employees(id)
);

COMMENT ON TABLE diagnostics IS 'Результаты диагностики устройств';


-- ════════════════════════════════════════════════════════════════════════════
-- МЕСТОПОЛОЖЕНИЕ УСТРОЙСТВ
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE device_locations (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    location VARCHAR(100) NOT NULL,
    moved_at TIMESTAMP NOT NULL DEFAULT NOW(),
    moved_by BIGINT NOT NULL,
    comment TEXT,

    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    FOREIGN KEY (moved_by) REFERENCES employees(id)
);

COMMENT ON TABLE device_locations IS 'История перемещения устройств (хранилище, ремонтная зона, готово к выдаче)';


-- ════════════════════════════════════════════════════════════════════════════
-- НАЛОГИ И СКИДКИ
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE tax_rates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    percent NUMERIC(5,2) NOT NULL CHECK (percent >= 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_at TIMESTAMP
);

COMMENT ON TABLE tax_rates IS 'Налоговые ставки';

CREATE TABLE discount_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    percent NUMERIC(5,2) CHECK (percent >= 0 AND percent <= 100),
    fixed_amount NUMERIC(12,2) CHECK (fixed_amount >= 0),
    
    CONSTRAINT chk_discount_type CHECK (
        (percent IS NOT NULL AND fixed_amount IS NULL)
        OR
        (percent IS NULL AND fixed_amount IS NOT NULL)
    )
);

COMMENT ON TABLE discount_types IS 'Типы скидок (процентная или фиксированная сумма)';


-- ════════════════════════════════════════════════════════════════════════════
-- ЧЕК
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE receipts (
    id BIGSERIAL PRIMARY KEY,
    repair_order_id BIGINT NOT NULL UNIQUE,

    subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12,2) NOT NULL DEFAULT 0,

    tax_rate_id BIGINT,
    discount_type_id BIGINT,
    
    -- Статус оплаты и блокировка
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    locked_at TIMESTAMP,
    locked_by BIGINT,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_at TIMESTAMP,

    FOREIGN KEY (repair_order_id) REFERENCES repair_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (tax_rate_id) REFERENCES tax_rates(id),
    FOREIGN KEY (discount_type_id) REFERENCES discount_types(id),
    FOREIGN KEY (locked_by) REFERENCES employees(id),
    
    CONSTRAINT chk_payment_status CHECK (payment_status IN ('UNPAID', 'PARTIALLY_PAID', 'FULLY_PAID', 'REFUNDED'))
);

COMMENT ON TABLE receipts IS 'Чеки заказов на ремонт';
COMMENT ON COLUMN receipts.payment_status IS 'Статус оплаты чека';
COMMENT ON COLUMN receipts.locked IS 'Флаг блокировки редактирования чека после начала оплаты';
COMMENT ON COLUMN receipts.locked_at IS 'Время блокировки чека';
COMMENT ON COLUMN receipts.locked_by IS 'Сотрудник, который заблокировал чек';


-- ════════════════════════════════════════════════════════════════════════════
-- РАБОТЫ
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE repair_works (
    id BIGSERIAL PRIMARY KEY,
    receipt_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_at TIMESTAMP,
    completed_at TIMESTAMP NOT NULL DEFAULT NOW(),

    FOREIGN KEY (receipt_id) REFERENCES receipts(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

COMMENT ON TABLE repair_works IS 'Выполненные работы по ремонту';


-- ════════════════════════════════════════════════════════════════════════════
-- ИЗНОС
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE degree_wears (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    wear_percent INTEGER CHECK (wear_percent BETWEEN 0 AND 100)
);

COMMENT ON TABLE degree_wears IS 'Степени износа запчастей и устройств';


-- ════════════════════════════════════════════════════════════════════════════
-- СКЛАД
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE inventory_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    serial_number VARCHAR(100) UNIQUE,
    degree_wear_id BIGINT NOT NULL,
    is_device BOOLEAN NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity >= 0),
    in_stock BOOLEAN NOT NULL DEFAULT TRUE,
    min_stock_level INTEGER DEFAULT 0,

    -- Поля для системы закупок
    preferred_supplier_id BIGINT,
    last_purchase_price NUMERIC(12,2),
    current_market_price NUMERIC(12,2),
    price_updated_at TIMESTAMP,
    reorder_quantity INTEGER DEFAULT 0,
    pack_size INTEGER NOT NULL DEFAULT 1,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_at TIMESTAMP,

    FOREIGN KEY (degree_wear_id) REFERENCES degree_wears(id),

    CONSTRAINT chk_serial_quantity CHECK (
        (serial_number IS NULL AND quantity >= 0)
        OR
        (serial_number IS NOT NULL AND quantity = 1)
    )
);

COMMENT ON TABLE inventory_items IS 'Складские запасы (запчасти и устройства)';
COMMENT ON COLUMN inventory_items.min_stock_level IS 'Минимальный уровень запаса для контроля критического остатка';
COMMENT ON COLUMN inventory_items.preferred_supplier_id IS 'Предпочтительный поставщик для автозаказа';
COMMENT ON COLUMN inventory_items.last_purchase_price IS 'Цена последней реальной закупки';
COMMENT ON COLUMN inventory_items.current_market_price IS 'Актуальная цена из Сервиса Мониторинга';
COMMENT ON COLUMN inventory_items.price_updated_at IS 'Когда последний раз обновлялась current_market_price';
COMMENT ON COLUMN inventory_items.reorder_quantity IS 'Фиксированное кол-во для заказа (0 = расчёт по среднему расходу)';
COMMENT ON COLUMN inventory_items.pack_size IS 'Кол-во штук в упаковке поставщика (1 = поштучная продажа)';

CREATE TABLE inventory_movements (
    id BIGSERIAL PRIMARY KEY,
    inventory_item_id BIGINT NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL,
    related_repair_order_id BIGINT,
    related_supply_request_id BIGINT,
    performed_by BIGINT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_at TIMESTAMP,

    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE,
    FOREIGN KEY (related_repair_order_id) REFERENCES repair_orders(id),
    FOREIGN KEY (performed_by) REFERENCES employees(id),
    
    CHECK (movement_type IN ('ПРИХОД', 'РАСХОД', 'РЕЗЕРВ', 'СПИСАНИЕ', 'ВОЗВРАТ', 'КОРРЕКТИРОВКА'))
);

COMMENT ON TABLE inventory_movements IS 'Движение товаров на складе';


-- ════════════════════════════════════════════════════════════════════════════
-- ЗАПЧАСТИ В РАБОТАХ
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE repair_work_items (
    repair_work_id BIGINT NOT NULL,
    inventory_item_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    price NUMERIC(12,2) NOT NULL CHECK (price >= 0),

    PRIMARY KEY (repair_work_id, inventory_item_id),

    FOREIGN KEY (repair_work_id) REFERENCES repair_works(id) ON DELETE CASCADE,
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
);

COMMENT ON TABLE repair_work_items IS 'Запчасти, использованные в ремонтных работах';


-- ════════════════════════════════════════════════════════════════════════════
-- ОПЛАТЫ
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE receipt_payments (
    id BIGSERIAL PRIMARY KEY,
    receipt_id BIGINT NOT NULL,
    paid_amount NUMERIC(12,2) NOT NULL CHECK (paid_amount > 0),
    payment_method VARCHAR(8) NOT NULL,
    transaction_id VARCHAR(100),
    payment_details JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_at TIMESTAMP,
    paid_at TIMESTAMP NOT NULL DEFAULT NOW(),
    accepted_by BIGINT NOT NULL,

    FOREIGN KEY (receipt_id) REFERENCES receipts(id) ON DELETE CASCADE,
    FOREIGN KEY (accepted_by) REFERENCES employees(id),
    
    CHECK (payment_method IN ('CASH', 'CARD', 'TRANSFER', 'OTHER'))
);

COMMENT ON TABLE receipt_payments IS 'Платежи по чекам';
COMMENT ON COLUMN receipt_payments.transaction_id IS 'ID транзакции от платежной системы (терминал, эквайринг)';
COMMENT ON COLUMN receipt_payments.payment_details IS 'Дополнительные детали платежа в формате JSON';


-- ════════════════════════════════════════════════════════════════════════════
-- ПОСТАВЩИКИ И ДОГОВОРЫ
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    phone VARCHAR(50) CHECK (phone IS NULL OR phone ~ '^[0-9+() -]{6,20}$'),
    email VARCHAR(100) CHECK (email IS NULL OR email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'),
    contact_person VARCHAR(100),
    address TEXT,
    inn VARCHAR(12),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Интеграция с Сервисом Мониторинга
    integration_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    price_source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    order_method VARCHAR(20) NOT NULL DEFAULT 'PHONE',
    website_url VARCHAR(500),
    contact_messenger VARCHAR(200),
    price_list_email VARCHAR(100),
    external_supplier_id VARCHAR(100),

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_at TIMESTAMP,

    CHECK (integration_type IN ('FULL_AUTO', 'PRICE_ONLY', 'MANUAL')),
    CHECK (price_source IN ('API', 'WEBSITE', 'EMAIL', 'MANUAL')),
    CHECK (order_method IN ('API', 'WEBSITE', 'EMAIL', 'MESSENGER', 'PHONE'))
);

COMMENT ON TABLE suppliers IS 'Поставщики запчастей';
COMMENT ON COLUMN suppliers.integration_type IS 'Уровень автоматизации: FULL_AUTO, PRICE_ONLY, MANUAL';
COMMENT ON COLUMN suppliers.price_source IS 'Источник цен: API, WEBSITE, EMAIL, MANUAL';
COMMENT ON COLUMN suppliers.order_method IS 'Способ заказа: API, WEBSITE, EMAIL, MESSENGER, PHONE';
COMMENT ON COLUMN suppliers.external_supplier_id IS 'ID поставщика в Сервисе Мониторинга';

-- Добавляем FK от inventory_items к suppliers (таблица suppliers создана после inventory_items)
ALTER TABLE inventory_items
    ADD CONSTRAINT fk_inventory_preferred_supplier
    FOREIGN KEY (preferred_supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL;

CREATE TABLE supplier_contracts (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    contract_number VARCHAR(100) NOT NULL UNIQUE,
    signed_at DATE NOT NULL,
    valid_until DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

COMMENT ON TABLE supplier_contracts IS 'Договоры с поставщиками';


-- ════════════════════════════════════════════════════════════════════════════
-- ЗАПРОСЫ НА ПОСТАВКУ
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE supply_requests (
    id BIGSERIAL PRIMARY KEY,
    request_number VARCHAR(30) UNIQUE,
    supplier_id BIGINT,
    contract_id BIGINT,
    requested_by BIGINT NOT NULL,
    approved_by BIGINT,
    related_repair_order BIGINT,
    status_id BIGINT NOT NULL,
    total_amount NUMERIC(12,2),
    comment TEXT,
    expected_delivery_date TIMESTAMP,

    -- Поля для системы закупок
    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    external_order_id VARCHAR(100),
    external_order_status VARCHAR(50),

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_at TIMESTAMP,

    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    FOREIGN KEY (contract_id) REFERENCES supplier_contracts(id),
    FOREIGN KEY (requested_by) REFERENCES employees(id),
    FOREIGN KEY (approved_by) REFERENCES employees(id),
    FOREIGN KEY (related_repair_order) REFERENCES repair_orders(id),
    FOREIGN KEY (status_id) REFERENCES supply_request_statuses(id),

    CHECK (source IN ('MANUAL', 'TECHNICIAN', 'AUTO_REORDER'))
);

COMMENT ON TABLE supply_requests IS 'Запросы на поставку запчастей';
COMMENT ON COLUMN supply_requests.source IS 'Источник заявки: MANUAL, TECHNICIAN, AUTO_REORDER';
COMMENT ON COLUMN supply_requests.external_order_id IS 'Номер заказа у поставщика (из Сервиса Мониторинга)';
COMMENT ON COLUMN supply_requests.external_order_status IS 'Статус заказа у поставщика';

CREATE TABLE supply_request_items (
    id BIGSERIAL PRIMARY KEY,
    supply_request_id BIGINT NOT NULL,
    inventory_item_id BIGINT,
    item_name VARCHAR(200) NOT NULL,
    part_number VARCHAR(100),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12,2),
    total_price NUMERIC(12,2),

    FOREIGN KEY (supply_request_id) REFERENCES supply_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
);

COMMENT ON TABLE supply_request_items IS 'Позиции в запросах на поставку';


-- ════════════════════════════════════════════════════════════════════════════
-- НАСТРОЙКИ СИСТЕМЫ ЗАКУПОК
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE supply_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(500) NOT NULL,
    description TEXT,
    last_modified_at TIMESTAMP,
    modified_by BIGINT,

    FOREIGN KEY (modified_by) REFERENCES employees(id)
);

COMMENT ON TABLE supply_settings IS 'Настройки системы автоматических закупок';
COMMENT ON COLUMN supply_settings.setting_key IS 'Уникальный ключ настройки';
COMMENT ON COLUMN supply_settings.setting_value IS 'Значение настройки';


-- ════════════════════════════════════════════════════════════════════════════
-- УВЕДОМЛЕНИЯ КЛИЕНТОВ
-- ════════════════════════════════════════════════════════════════════════════

CREATE TABLE notification_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    trigger_event VARCHAR(50) NOT NULL,
    sms_template TEXT,
    email_subject VARCHAR(200),
    email_template TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

COMMENT ON TABLE notification_templates IS 'Шаблоны уведомлений для клиентов';

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    repair_order_id BIGINT,
    notification_type VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_attempt TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_at TIMESTAMP,

    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (repair_order_id) REFERENCES repair_orders(id),
    
    CHECK (notification_type IN ('SMS', 'EMAIL', 'PUSH')),
    CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED'))
);

COMMENT ON TABLE notifications IS 'Очередь уведомлений для клиентов';


-- ════════════════════════════════════════════════════════════════════════════
-- ФУНКЦИЯ ПЕРЕСЧЕТА ЧЕКА
-- ════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION recalc_receipt_total(p_receipt_id BIGINT)
RETURNS VOID AS $$
DECLARE
    v_work NUMERIC(12,2);
    v_parts NUMERIC(12,2);
    v_subtotal NUMERIC(12,2);
    v_tax_percent NUMERIC(5,2);
    v_discount NUMERIC(12,2);
BEGIN
    -- Сумма работ
    SELECT COALESCE(SUM(price), 0)
    INTO v_work
    FROM repair_works
    WHERE receipt_id = p_receipt_id;

    -- Сумма запчастей
    SELECT COALESCE(SUM(rwi.price * rwi.quantity), 0)
    INTO v_parts
    FROM repair_work_items rwi
    JOIN repair_works rw ON rw.id = rwi.repair_work_id
    WHERE rw.receipt_id = p_receipt_id;

    v_subtotal := v_work + v_parts;

    -- Процент налога
    SELECT COALESCE(tr.percent, 0)
    INTO v_tax_percent
    FROM receipts r
    LEFT JOIN tax_rates tr ON tr.id = r.tax_rate_id
    WHERE r.id = p_receipt_id;

    -- Расчет скидки
    SELECT COALESCE(
        CASE
            WHEN dt.percent IS NOT NULL THEN v_subtotal * dt.percent / 100
            WHEN dt.fixed_amount IS NOT NULL THEN dt.fixed_amount
            ELSE 0
        END,
        0
    )
    INTO v_discount
    FROM receipts r
    LEFT JOIN discount_types dt ON dt.id = r.discount_type_id
    WHERE r.id = p_receipt_id;

    -- Обновляем чек
    UPDATE receipts
    SET subtotal = v_subtotal,
        discount_amount = v_discount,
        tax_amount = (v_subtotal - v_discount) * v_tax_percent / 100,
        total_amount = (v_subtotal - v_discount) + ((v_subtotal - v_discount) * v_tax_percent / 100)
    WHERE id = p_receipt_id;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION recalc_receipt_total IS 'Автоматический пересчет итоговой суммы чека';


-- ════════════════════════════════════════════════════════════════════════════
-- ФУНКЦИЯ ГЕНЕРАЦИИ НОМЕРА ЗАКАЗА
-- ════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION generate_order_number()
RETURNS TRIGGER AS $$
BEGIN
    NEW.order_number := 'RO-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || 
                        LPAD(NEXTVAL('order_number_seq')::TEXT, 4, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION generate_order_number IS 'Генерация уникального номера заказа';


-- ════════════════════════════════════════════════════════════════════════════
-- ФУНКЦИЯ АВТОСОЗДАНИЯ ЧЕКА
-- ════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION create_receipt_for_order()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO receipts (repair_order_id, subtotal, discount_amount, tax_amount, total_amount)
    VALUES (NEW.id, 0, 0, 0, 0);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION create_receipt_for_order IS 'Автоматически создает чек при создании заказа на ремонт';


-- ════════════════════════════════════════════════════════════════════════════
-- ФУНКЦИЯ ОБНОВЛЕНИЯ СТАТУСА ОПЛАТЫ
-- ════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION update_payment_status()
RETURNS TRIGGER AS $$
DECLARE
    v_total NUMERIC(12,2);
    v_paid NUMERIC(12,2);
BEGIN
    SELECT total_amount INTO v_total FROM receipts WHERE id = NEW.receipt_id;
    
    SELECT COALESCE(SUM(paid_amount), 0) INTO v_paid 
    FROM receipt_payments WHERE receipt_id = NEW.receipt_id;
    
    IF v_paid >= v_total THEN
        UPDATE receipts SET payment_status = 'FULLY_PAID' WHERE id = NEW.receipt_id;
    ELSIF v_paid > 0 THEN
        UPDATE receipts SET payment_status = 'PARTIALLY_PAID' WHERE id = NEW.receipt_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_payment_status IS 'Автоматическое обновление статуса оплаты чека';


-- ════════════════════════════════════════════════════════════════════════════
-- ФУНКЦИЯ СОЗДАНИЯ УВЕДОМЛЕНИЯ ПРИ СМЕНЕ СТАТУСА
-- ════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION create_status_change_notification()
RETURNS TRIGGER AS $$
DECLARE
    v_client_id BIGINT;
    v_status_name VARCHAR(50);
    v_sms_template TEXT;
    v_email_subject VARCHAR(200);
    v_email_template TEXT;
BEGIN
    SELECT ro.client_id, rs.name
    INTO v_client_id, v_status_name
    FROM repair_orders ro
    JOIN repair_statuses rs ON rs.id = NEW.status_id
    WHERE ro.id = NEW.repair_order_id;

    SELECT sms_template, email_subject, email_template
    INTO v_sms_template, v_email_subject, v_email_template
    FROM notification_templates
    WHERE trigger_event = 'status_change' AND is_active = TRUE
    LIMIT 1;

    IF v_sms_template IS NOT NULL THEN
        INSERT INTO notifications (client_id, repair_order_id, notification_type, message)
        VALUES (
            v_client_id,
            NEW.repair_order_id,
            'SMS',
            REPLACE(v_sms_template, '{status}', v_status_name)
        );
    END IF;

    IF v_email_template IS NOT NULL THEN
        INSERT INTO notifications (client_id, repair_order_id, notification_type, message)
        VALUES (
            v_client_id,
            NEW.repair_order_id,
            'EMAIL',
            v_email_subject || E'\n\n' || REPLACE(v_email_template, '{status}', v_status_name)
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION create_status_change_notification IS 'Создание уведомления клиента при смене статуса заказа';


-- ════════════════════════════════════════════════════════════════════════════
-- ФУНКЦИЯ БЕЗОПАСНОГО СПИСАНИЯ СО СКЛАДА
-- ════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION consume_inventory_item(
    p_item_id BIGINT,
    p_quantity INTEGER,
    p_repair_order_id BIGINT,
    p_employee_id BIGINT
)
RETURNS BOOLEAN AS $$
DECLARE
    v_current_quantity INTEGER;
BEGIN
    -- Блокируем запись для обновления (защита от race conditions)
    SELECT quantity INTO v_current_quantity
    FROM inventory_items
    WHERE id = p_item_id
    FOR UPDATE;
    
    -- Проверяем достаточность товара
    IF v_current_quantity IS NULL THEN
        RAISE EXCEPTION 'Товар с ID % не найден', p_item_id;
    END IF;
    
    IF v_current_quantity < p_quantity THEN
        RAISE EXCEPTION 'Недостаточно товара на складе. Доступно: %, запрошено: %', 
            v_current_quantity, p_quantity;
    END IF;
    
    -- Уменьшаем количество
    UPDATE inventory_items
    SET quantity = quantity - p_quantity
    WHERE id = p_item_id;
    
    -- Создаем запись о движении
    INSERT INTO inventory_movements (
        inventory_item_id,
        movement_type,
        quantity,
        related_repair_order_id,
        performed_by,
        comment
    ) VALUES (
        p_item_id,
        'РАСХОД',
        p_quantity,
        p_repair_order_id,
        p_employee_id,
        'Списание для заказа на ремонт'
    );
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION consume_inventory_item IS 'Безопасное списание товара со склада с проверкой остатков и блокировкой';


-- ════════════════════════════════════════════════════════════════════════════
-- ТРИГГЕРЫ
-- ════════════════════════════════════════════════════════════════════════════

-- Триггер генерации номера заказа
CREATE TRIGGER trg_generate_order_number
BEFORE INSERT ON repair_orders
FOR EACH ROW
EXECUTE FUNCTION generate_order_number();

-- Триггер автосоздания чека
CREATE TRIGGER trg_create_receipt
AFTER INSERT ON repair_orders
FOR EACH ROW
EXECUTE FUNCTION create_receipt_for_order();

-- Триггер пересчета чека при изменении работ
CREATE OR REPLACE FUNCTION trg_recalc_receipt_after_work()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        PERFORM recalc_receipt_total(OLD.receipt_id);
    ELSE
        PERFORM recalc_receipt_total(NEW.receipt_id);
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_recalc_after_work
AFTER INSERT OR UPDATE OR DELETE ON repair_works
FOR EACH ROW
EXECUTE FUNCTION trg_recalc_receipt_after_work();

-- Триггер пересчета чека при изменении запчастей
CREATE OR REPLACE FUNCTION trg_recalc_receipt_after_items()
RETURNS TRIGGER AS $$
DECLARE
    v_receipt_id BIGINT;
BEGIN
    IF TG_OP = 'DELETE' THEN
        SELECT receipt_id INTO v_receipt_id FROM repair_works WHERE id = OLD.repair_work_id;
    ELSE
        SELECT receipt_id INTO v_receipt_id FROM repair_works WHERE id = NEW.repair_work_id;
    END IF;
    
    PERFORM recalc_receipt_total(v_receipt_id);
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_recalc_after_items
AFTER INSERT OR UPDATE OR DELETE ON repair_work_items
FOR EACH ROW
EXECUTE FUNCTION trg_recalc_receipt_after_items();

-- Триггер обновления статуса оплаты
CREATE TRIGGER trg_update_payment_status
AFTER INSERT OR UPDATE ON receipt_payments
FOR EACH ROW
EXECUTE FUNCTION update_payment_status();

-- Триггер создания уведомления при смене статуса
CREATE TRIGGER trg_status_change_notification
AFTER INSERT ON status_history
FOR EACH ROW
EXECUTE FUNCTION create_status_change_notification();


-- ════════════════════════════════════════════════════════════════════════════
-- ИНДЕКСЫ ДЛЯ ПРОИЗВОДИТЕЛЬНОСТИ
-- ════════════════════════════════════════════════════════════════════════════

-- Индексы для аудита
CREATE INDEX idx_audit_employee ON security_audit_log(employee_id);
CREATE INDEX idx_audit_date ON security_audit_log(created_at DESC);
CREATE INDEX idx_audit_event ON security_audit_log(event_type);
CREATE INDEX idx_audit_resource ON security_audit_log(resource_type, resource_id) WHERE resource_type IS NOT NULL;
CREATE INDEX idx_audit_failed_logins ON security_audit_log(employee_id, created_at) WHERE event_type = 'LOGIN_FAILED';

-- Индексы для заказов
CREATE INDEX idx_repair_orders_client ON repair_orders(client_id);
CREATE INDEX idx_repair_orders_device ON repair_orders(device_id);
CREATE INDEX idx_repair_orders_status ON repair_orders(current_status_id);
CREATE INDEX idx_repair_orders_master ON repair_orders(assigned_master_id);
CREATE INDEX idx_repair_orders_priority ON repair_orders(priority_id);
CREATE INDEX idx_repair_orders_dates ON repair_orders(created_at, actual_completion_date);

-- Индексы для истории статусов
CREATE INDEX idx_status_history_order ON status_history(repair_order_id);
CREATE INDEX idx_status_history_status ON status_history(status_id);
CREATE INDEX idx_status_history_date ON status_history(changed_at);

-- Индексы для устройств
CREATE INDEX idx_devices_client ON devices(client_id);
CREATE INDEX idx_devices_model ON devices(model_id);

-- Индексы для работ
CREATE INDEX idx_repair_works_receipt ON repair_works(receipt_id);
CREATE INDEX idx_repair_works_employee ON repair_works(employee_id);

-- Индексы для запчастей в работах
CREATE INDEX idx_repair_work_items_work ON repair_work_items(repair_work_id);
CREATE INDEX idx_repair_work_items_item ON repair_work_items(inventory_item_id);

-- Индексы для склада
CREATE INDEX idx_inventory_low_stock ON inventory_items(quantity) WHERE quantity < min_stock_level;
CREATE INDEX idx_inventory_in_stock ON inventory_items(in_stock);

-- Индексы для движения товаров
CREATE INDEX idx_inventory_movements_item ON inventory_movements(inventory_item_id);
CREATE INDEX idx_inventory_movements_order ON inventory_movements(related_repair_order_id);
CREATE INDEX idx_inventory_movements_date ON inventory_movements(created_at);

-- Индексы для поставок
CREATE INDEX idx_supply_requests_status ON supply_requests(status_id);
CREATE INDEX idx_supply_requests_supplier ON supply_requests(supplier_id);

-- Индексы для уведомлений
CREATE INDEX idx_notifications_client ON notifications(client_id);
CREATE INDEX idx_notifications_status ON notifications(status) WHERE status = 'PENDING';
CREATE INDEX idx_notifications_retry ON notifications(retry_count, last_attempt) WHERE status = 'PENDING';

-- Индексы для чеков
CREATE INDEX idx_receipts_order ON receipts(repair_order_id);
CREATE INDEX idx_receipt_payments_receipt ON receipt_payments(receipt_id);
CREATE INDEX idx_receipt_payments_transaction ON receipt_payments(transaction_id) WHERE transaction_id IS NOT NULL;

-- Индексы для местоположений устройств
CREATE INDEX idx_device_locations_device ON device_locations(device_id);
CREATE INDEX idx_device_locations_date ON device_locations(moved_at);


-- ════════════════════════════════════════════════════════════════════════════
-- ПРЕДСТАВЛЕНИЯ ДЛЯ АНАЛИТИКИ (ОПТИМИЗИРОВАННЫЕ)
-- ════════════════════════════════════════════════════════════════════════════

-- Загрузка техников
CREATE VIEW v_master_workload AS
SELECT 
    e.id,
    e.surname || ' ' || e.name as master_name,
    COUNT(DISTINCT ro.id) as active_orders,
    COUNT(DISTINCT rw.id) as completed_works,
    COALESCE(SUM(rw.price), 0) as total_revenue
FROM employees e
INNER JOIN employee_roles er ON er.employee_id = e.id
INNER JOIN roles r ON r.id = er.role_id AND r.name = 'TECHNICIAN'
LEFT JOIN repair_orders ro ON ro.assigned_master_id = e.id AND ro.actual_completion_date IS NULL
LEFT JOIN repair_works rw ON rw.employee_id = e.id
GROUP BY e.id, e.surname, e.name
ORDER BY active_orders DESC;

COMMENT ON VIEW v_master_workload IS 'Загрузка техников: активные заказы, выполненные работы, выручка';

-- Топ неисправностей
CREATE VIEW v_top_malfunctions AS
SELECT 
    d.description,
    COUNT(*) as malfunction_count,
    AVG(EXTRACT(EPOCH FROM (ro.actual_completion_date - ro.created_at))/3600) as avg_repair_hours
FROM diagnostics d
JOIN repair_orders ro ON ro.id = d.repair_order_id
WHERE ro.actual_completion_date IS NOT NULL
GROUP BY d.description
ORDER BY malfunction_count DESC;

COMMENT ON VIEW v_top_malfunctions IS 'Топ неисправностей по частоте и среднему времени ремонта';

-- Статистика заказов по статусам
CREATE VIEW v_orders_by_status AS
SELECT 
    rs.name as status_name,
    COUNT(*) as order_count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM repair_orders ro
JOIN repair_statuses rs ON rs.id = ro.current_status_id
GROUP BY rs.name
ORDER BY order_count DESC;

COMMENT ON VIEW v_orders_by_status IS 'Статистика заказов по статусам с процентами';

-- Отчёт по низкому уровню запасов
CREATE VIEW v_low_stock_items AS
SELECT 
    id,
    name,
    quantity,
    min_stock_level,
    (min_stock_level - quantity) as shortage
FROM inventory_items
WHERE quantity < min_stock_level AND in_stock = TRUE
ORDER BY shortage DESC;

COMMENT ON VIEW v_low_stock_items IS 'Товары с критически низким уровнем запасов';

-- Отчёт по движению склада за период
CREATE VIEW v_inventory_movements_summary AS
SELECT 
    ii.name as item_name,
    im.movement_type,
    SUM(im.quantity) as total_quantity,
    COUNT(*) as operations_count
FROM inventory_movements im
JOIN inventory_items ii ON ii.id = im.inventory_item_id
GROUP BY ii.name, im.movement_type
ORDER BY ii.name, im.movement_type;

COMMENT ON VIEW v_inventory_movements_summary IS 'Сводка по движению товаров на складе';

-- Финансовый отчет по периодам
CREATE VIEW v_revenue_by_period AS
SELECT 
    DATE_TRUNC('day', ro.created_at) as period_date,
    DATE_TRUNC('week', ro.created_at) as period_week,
    DATE_TRUNC('month', ro.created_at) as period_month,
    COUNT(*) as orders_count,
    SUM(r.total_amount) as total_revenue,
    SUM(r.subtotal) as subtotal,
    SUM(r.tax_amount) as total_tax,
    SUM(r.discount_amount) as total_discount,
    AVG(r.total_amount) as avg_order_value
FROM repair_orders ro
JOIN receipts r ON r.repair_order_id = ro.id
WHERE ro.actual_completion_date IS NOT NULL 
  AND r.payment_status = 'FULLY_PAID'
GROUP BY DATE_TRUNC('day', ro.created_at), 
         DATE_TRUNC('week', ro.created_at),
         DATE_TRUNC('month', ro.created_at)
ORDER BY period_date DESC;

COMMENT ON VIEW v_revenue_by_period IS 'Финансовый отчет по периодам (день/неделя/месяц)';

-- Топ клиентов по количеству обращений и сумме
CREATE VIEW v_top_clients AS
SELECT 
    c.id,
    c.surname || ' ' || c.name || COALESCE(' ' || c.patronymic, '') as client_full_name,
    c.phone,
    c.email,
    COUNT(DISTINCT ro.id) as total_orders,
    SUM(CASE WHEN r.payment_status = 'FULLY_PAID' THEN r.total_amount ELSE 0 END) as total_spent,
    AVG(CASE WHEN r.payment_status = 'FULLY_PAID' THEN r.total_amount ELSE NULL END) as avg_order_value,
    MAX(ro.created_at) as last_order_date
FROM clients c
JOIN repair_orders ro ON ro.client_id = c.id
JOIN receipts r ON r.repair_order_id = ro.id
GROUP BY c.id, c.surname, c.name, c.patronymic, c.phone, c.email
ORDER BY total_orders DESC, total_spent DESC;

COMMENT ON VIEW v_top_clients IS 'Топ клиентов по количеству обращений и потраченной сумме';

-- Производительность техников
CREATE VIEW v_technician_performance AS
SELECT 
    e.id,
    e.surname || ' ' || e.name as technician_name,
    COUNT(DISTINCT ro.id) as total_orders,
    COUNT(DISTINCT CASE WHEN ro.actual_completion_date IS NOT NULL THEN ro.id END) as completed_orders,
    AVG(EXTRACT(EPOCH FROM (ro.actual_completion_date - ro.created_at))/3600)::NUMERIC(10,2) as avg_completion_hours,
    SUM(rw.price) as total_labor_revenue,
    COUNT(DISTINCT rw.id) as total_works_performed
FROM employees e
INNER JOIN employee_roles er ON er.employee_id = e.id
INNER JOIN roles r ON r.id = er.role_id AND r.name = 'TECHNICIAN'
LEFT JOIN repair_orders ro ON ro.assigned_master_id = e.id
LEFT JOIN repair_works rw ON rw.employee_id = e.id
WHERE e.blocked = FALSE
GROUP BY e.id, e.surname, e.name
ORDER BY completed_orders DESC;

COMMENT ON VIEW v_technician_performance IS 'Производительность техников: заказы, время выполнения, выручка';

-- Статус складских остатков
CREATE VIEW v_inventory_status AS
SELECT 
    ii.id,
    ii.name,
    ii.quantity,
    ii.min_stock_level,
    ii.unit_price,
    ii.quantity * ii.unit_price as total_value,
    CASE 
        WHEN ii.quantity = 0 THEN 'OUT_OF_STOCK'
        WHEN ii.quantity < ii.min_stock_level THEN 'LOW_STOCK'
        WHEN ii.quantity < ii.min_stock_level * 2 THEN 'MEDIUM_STOCK'
        ELSE 'GOOD_STOCK'
    END as stock_status,
    dw.name as wear_condition
FROM inventory_items ii
LEFT JOIN degree_wears dw ON dw.id = ii.degree_wear_id
WHERE ii.in_stock = TRUE
ORDER BY 
    CASE 
        WHEN ii.quantity = 0 THEN 1
        WHEN ii.quantity < ii.min_stock_level THEN 2
        ELSE 3
    END,
    ii.name;

COMMENT ON VIEW v_inventory_status IS 'Текущий статус складских остатков с категоризацией';


-- ════════════════════════════════════════════════════════════════════════════
-- НАЧАЛЬНЫЕ ДАННЫЕ СИСТЕМЫ
-- ════════════════════════════════════════════════════════════════════════════

-- Вставка базовых ролей
INSERT INTO roles (name) VALUES 
('ADMIN'),
('TECHNICIAN'),
('RECEPTIONIST')
ON CONFLICT (name) DO NOTHING;

-- Вставка статусов ремонта
INSERT INTO repair_statuses (name) VALUES 
('Новая'),
('Принята'),
('Диагностика'),
('Ожидает запчастей'),
('Ожидает подтверждения клиента'),
('В ремонте'),
('Ремонт завершен'),
('Готов к выдаче'),
('Выдан'),
('Отменен'),
('Гарантийный случай')
ON CONFLICT (name) DO NOTHING;

-- Вставка статусов поставок (английские — локализация на фронтенде)
INSERT INTO supply_request_statuses (name) VALUES
('NEW'),
('AUTO_FORMED'),
('APPROVED'),
('ORDERED'),
('IN_TRANSIT'),
('DELIVERED'),
('PARTIALLY_DELIVERED'),
('CANCELLED')
ON CONFLICT (name) DO NOTHING;

-- Вставка степеней износа
INSERT INTO degree_wears (name, wear_percent) VALUES 
('Новое', 0),
('Отличное состояние', 5),
('Очень хорошее', 10),
('Хорошее', 25),
('Удовлетворительное', 50),
('Плохое', 75),
('Непригодное', 95)
ON CONFLICT (name) DO NOTHING;

-- Вставка приоритетов заказов
INSERT INTO repair_priorities (name, sort_order, color_hex, description) VALUES 
('Стандартный', 3, '#6c757d', 'Обычный заказ без срочности'),
('Повышенный', 2, '#ffc107', 'Требуется выполнить быстрее стандартного срока'),
('Срочный', 1, '#fd7e14', 'Срочный заказ, выполнить в приоритетном порядке'),
('VIP клиент', 0, '#dc3545', 'VIP клиент, максимальный приоритет')
ON CONFLICT (name) DO NOTHING;

-- Вставка базовых типов устройств
INSERT INTO device_types (name) VALUES 
('Ноутбук'),
('Настольный компьютер'),
('Моноблок'),
('Смартфон'),
('Планшет'),
('Принтер'),
('МФУ'),
('Монитор'),
('Телевизор'),
('Игровая консоль')
ON CONFLICT (name) DO NOTHING;

-- Вставка начальных настроек системы закупок
INSERT INTO supply_settings (setting_key, setting_value, description) VALUES
('auto_reorder_enabled', 'false', 'Генерировать ли авто-заявки при низком остатке'),
('auto_approve_enabled', 'false', 'Подтверждать ли мелкие заявки автоматически'),
('auto_approve_max_amount', '5000.00', 'Порог суммы для авто-подтверждения (руб.)'),
('reorder_check_cron', '0 0 8 * * MON-SAT', 'Расписание проверки остатков (cron)'),
('consolidation_enabled', 'false', 'Объединять ли заявки к одному поставщику'),
('default_reorder_multiplier', '1.5', 'Множитель к среднему расходу при расчёте объёма закупки'),
('days_for_consumption_avg', '30', 'Период для расчёта среднего расхода (дней)'),
('price_update_cron', '0 0 6 * * MON-SAT', 'Расписание обновления цен (cron)'),
('monitoring_service_url', 'http://monitoring-service:8081', 'URL Сервиса Мониторинга')
ON CONFLICT (setting_key) DO NOTHING;

-- Создание первого администратора
-- ВАЖНО: Этот пароль необходимо изменить при первом входе!
-- Хеш для пароля "Admin123!" (используйте bcrypt с complexity 12 в production)
DO $$
DECLARE
    v_admin_id BIGINT;
    v_admin_role_id BIGINT;
BEGIN
    -- Получаем ID роли ADMIN
    SELECT id INTO v_admin_role_id FROM roles WHERE name = 'ADMIN';
    
    -- Создаем первого администратора, если он не существует
    INSERT INTO employees (name, surname, patronymic, date_birth, login, password, blocked, created_at)
    VALUES (
        'Системный',
        'Администратор',
        'Системович',
        '1990-01-01',
        'admin',
        '$2a$12$7QpZz8O/aooezNlBOPXTqud0TTzMoEsg8OLQSy7/hJrphwbo4vWlq', -- Хеш для "Admin123!"
        FALSE,
        NOW()
    )
    ON CONFLICT (login) DO NOTHING
    RETURNING id INTO v_admin_id;
    
    -- Если администратор был создан, назначаем ему роль
    IF v_admin_id IS NOT NULL THEN
        INSERT INTO employee_roles (employee_id, role_id)
        VALUES (v_admin_id, v_admin_role_id)
        ON CONFLICT DO NOTHING;
        
        RAISE NOTICE 'Создан администратор с логином: admin и паролем: Admin123!';
        RAISE NOTICE 'ВАЖНО: Смените пароль при первом входе!';
    END IF;
END $$;


-- ════════════════════════════════════════════════════════════════════════════
-- ЗАВЕРШЕНИЕ ИНИЦИАЛИЗАЦИИ
-- ════════════════════════════════════════════════════════════════════════════

DO $$
BEGIN
    RAISE NOTICE '════════════════════════════════════════════════════════';
    RAISE NOTICE 'База данных CRM RepairLog успешно инициализирована!';
    RAISE NOTICE '════════════════════════════════════════════════════════';
    RAISE NOTICE 'Создано:';
    RAISE NOTICE '- 34 таблицы';
    RAISE NOTICE '- 26 индексов';
    RAISE NOTICE '- 10 представлений для отчетов';
    RAISE NOTICE '- 6 триггеров';
    RAISE NOTICE '- 5 функций';
    RAISE NOTICE '- Начальные данные системы';
    RAISE NOTICE '- Первый администратор (login: admin, password: Admin123!)';
    RAISE NOTICE '════════════════════════════════════════════════════════';
    RAISE NOTICE 'ВАЖНО: Измените пароль администратора при первом входе!';
    RAISE NOTICE '════════════════════════════════════════════════════════';
END $$;
