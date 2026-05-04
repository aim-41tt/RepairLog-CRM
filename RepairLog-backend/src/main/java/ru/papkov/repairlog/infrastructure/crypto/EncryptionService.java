package ru.papkov.repairlog.infrastructure.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptionService(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public String encrypt(String value) {

        if (value == null) {
            return null;
        }

        try {

            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(TAG_LENGTH, iv)
            );

            byte[] encrypted = cipher.doFinal(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            ByteBuffer buffer =
                    ByteBuffer.allocate(iv.length + encrypted.length);

            buffer.put(iv);
            buffer.put(encrypted);

            return Base64.getEncoder()
                    .encodeToString(buffer.array());

        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public String decrypt(String value) {

        if (value == null) {
            return null;
        }

        try {

            byte[] payload =
                    Base64.getDecoder().decode(value);

            ByteBuffer buffer = ByteBuffer.wrap(payload);

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            byte[] encrypted =
                    new byte[buffer.remaining()];

            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(TAG_LENGTH, iv)
            );

            return new String(
                    cipher.doFinal(encrypted),
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }
}