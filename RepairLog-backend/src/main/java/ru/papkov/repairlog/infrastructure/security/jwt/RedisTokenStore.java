package ru.papkov.repairlog.infrastructure.security.jwt;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Хранилище JWT токенов в Redis.
 * 
 * @author aim-41tt
 */
@Component
public class RedisTokenStore {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String TOKEN_PREFIX = "jwt:";

    public RedisTokenStore(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void store(String username, String token, Duration expiration) {
        String key = getKey(username);
        redisTemplate.opsForValue().set(key, token, expiration);
    }

    public boolean exists(String username, String token) {
        String key = getKey(username);
        String storedToken = redisTemplate.opsForValue().get(key);
        return token.equals(storedToken);
    }

    public void delete(String username) {
        String key = getKey(username);
        redisTemplate.delete(key);
    }

    private String getKey(String username) {
        return TOKEN_PREFIX + username;
    }
}
