package ru.papkov.repairlog.infrastructure.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * HTTP-клиент для Document API (генерация PDF-документов).
 *
 * @author aim-41tt
 */
@Component
public class DocumentApiClient {

    private static final Logger log = LoggerFactory.getLogger(DocumentApiClient.class);

    private final RestClient restClient;

    public DocumentApiClient(
            @Value("${app.document-service.base-url:http://localhost:8093}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public byte[] generateReceipt(Object request) {
        return postForPdf("/api/v1/documents/receipt", request);
    }

    public byte[] generateCompletionAct(Object request) {
        return postForPdf("/api/v1/documents/completion-act", request);
    }

    public byte[] generateWarrantyCard(Object request) {
        return postForPdf("/api/v1/documents/warranty-card", request);
    }

    public byte[] generateRejectionSheet(Object request) {
        return postForPdf("/api/v1/documents/rejection-sheet", request);
    }

    private byte[] postForPdf(String uri, Object request) {
        try {
            return restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientException e) {
            log.error("Ошибка генерации PDF ({}): {}", uri, e.getMessage());
            throw new RuntimeException("Сервис генерации документов недоступен", e);
        }
    }
}
