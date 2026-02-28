package ru.papkov.repairlog.infrastructure.integration;

import ru.papkov.repairlog.application.dto.monitoring.MonitoringOrderRequest;
import ru.papkov.repairlog.application.dto.monitoring.MonitoringOrderResponse;
import ru.papkov.repairlog.application.dto.monitoring.MonitoringPriceRequest;
import ru.papkov.repairlog.application.dto.monitoring.MonitoringPriceResponse;

/**
 * Интерфейс клиента для внешнего Сервиса Мониторинга цен.
 */
public interface MonitoringServiceClient {

    /**
     * Запросить актуальные цены у поставщика.
     */
    MonitoringPriceResponse fetchPrices(MonitoringPriceRequest request);

    /**
     * Создать заказ у поставщика через Сервис Мониторинга.
     */
    MonitoringOrderResponse placeOrder(MonitoringOrderRequest request);

    /**
     * Проверить доступность Сервиса Мониторинга.
     */
    boolean isAvailable();
}
