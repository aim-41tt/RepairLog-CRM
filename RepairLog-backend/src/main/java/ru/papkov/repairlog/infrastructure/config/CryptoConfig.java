package ru.papkov.repairlog.infrastructure.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CryptoConfig {

	@Value("${app.crypto.key}")
	private String base64Key;

	@Bean
	protected SecretKey secretKey() {

		byte[] decoded = Base64.getDecoder().decode(base64Key);

		return new SecretKeySpec(decoded, "AES");
	}
}