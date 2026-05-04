package ru.papkov.repairlog.infrastructure.crypto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Component
@Converter
public class EncryptConverter implements AttributeConverter<String, String> {

	private static EncryptionService encryptionService;

	@Autowired
	public void setEncryptionService(EncryptionService service) {
		EncryptConverter.encryptionService = service;
	}

	@Override
	public String convertToDatabaseColumn(String attribute) {
		return encryptionService.encrypt(attribute);
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		return encryptionService.decrypt(dbData);
	}
}