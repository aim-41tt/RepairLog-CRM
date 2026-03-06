package ru.papkov.repairlog.infrastructure.security.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Валидатор HMAC-SHA256 подписи вебхуков от Сервиса Мониторинга.
 * Проверяет заголовок X-Webhook-Signature для защиты от подделки запросов.
 *
 * @author aim-41tt
 */
@Component
public class WebhookSignatureValidator {

    private final String webhookSecret;

    public WebhookSignatureValidator(@Value("${app.webhook.secret:}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    /**
     * Проверить подпись вебхука.
     *
     * @param payload         тело запроса (raw JSON)
     * @param signatureHeader значение заголовка X-Webhook-Signature
     * @return true если подпись валидна
     */
    public boolean isValid(String payload, String signatureHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            // Если секрет не настроен — пропускаем проверку (dev-режим)
            return true;
        }
        if (signatureHeader == null || signatureHeader.isBlank()) {
            return false;
        }
        try {
            String expected = computeHmacSha256(payload);
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signatureHeader.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    private String computeHmacSha256(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
                webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
