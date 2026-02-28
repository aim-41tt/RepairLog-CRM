package ru.papkov.repairlog.infrastructure.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.papkov.repairlog.application.dto.monitoring.MonitoringOrderRequest;
import ru.papkov.repairlog.application.dto.monitoring.MonitoringOrderResponse;
import ru.papkov.repairlog.application.dto.monitoring.MonitoringPriceRequest;
import ru.papkov.repairlog.application.dto.monitoring.MonitoringPriceResponse;

/**
 * Реализация клиента Сервиса Мониторинга через Spring RestClient.
 */
@Component
public class MonitoringServiceRestClient implements MonitoringServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MonitoringServiceRestClient.class);

    private final RestClient restClient;

    public MonitoringServiceRestClient(
            @Value("${monitoring.service.base-url:http://localhost:8090}") String baseUrl,
            @Value("${monitoring.service.api-key:}") String apiKey) {

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl);

        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("X-API-Key", apiKey);
        }

        this.restClient = builder.build();
    }

    @Override
    public MonitoringPriceResponse fetchPrices(MonitoringPriceRequest request) {
        try {
            return restClient.post()
                    .uri("/api/prices/fetch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(MonitoringPriceResponse.class);
        } catch (RestClientException e) {
            log.error("Ошибка запроса цен от Сервиса Мониторинга: {}", e.getMessage());
            throw new RuntimeException("Сервис Мониторинга недоступен", e);
        }
    }

    @Override
    public MonitoringOrderResponse placeOrder(MonitoringOrderRequest request) {
        try {
            return restClient.post()
                    .uri("/api/orders/place")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(MonitoringOrderResponse.class);
        } catch (RestClientException e) {
            log.error("Ошибка размещения заказа через Сервис Мониторинга: {}", e.getMessage());
            throw new RuntimeException("Сервис Мониторинга недоступен", e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            restClient.get()
                    .uri("/api/health")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            log.warn("Сервис Мониторинга недоступен: {}", e.getMessage());
            return false;
        }
    }
}
