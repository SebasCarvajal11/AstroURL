package com.astrourl.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private final StringRedisTemplate redisTemplate;
    private final int limit;
    private static final String KEY_PREFIX = "rate_limit:ip:";
    private final Map<String, LocalRateLimitEntry> localCounters = new ConcurrentHashMap<>();

    // Constructor manual
    public RateLimitService(
            StringRedisTemplate redisTemplate,
            @Value("${app.limits.guest-weekly-limit:10}") int limit
    ) {
        this.redisTemplate = redisTemplate;
        this.limit = limit;
    }

    public boolean isLimitExceeded(String ip) {
        try {
            return isLimitExceededWithRedis(ip);
        } catch (Exception ex) {
            // Redis puede lanzar DataAccessException, TimeoutException u otras no envueltas;
            // sin esto el error comprobado llega al @RestControllerAdvice como 500 genérico.
            log.warn("Redis no disponible para rate limit; contador local. Causa: {}", ex.toString());
            return isLimitExceededInMemory(ip);
        }
    }

    private boolean isLimitExceededWithRedis(String ip) {
        String key = KEY_PREFIX + ip;
        String currentCount = redisTemplate.opsForValue().get(key);

        if (currentCount != null && Integer.parseInt(currentCount) >= limit) {
            return true;
        }

        if (currentCount == null) {
            redisTemplate.opsForValue().set(key, "1", 7, TimeUnit.DAYS);
        } else {
            redisTemplate.opsForValue().increment(key);
        }

        return false;
    }

    private boolean isLimitExceededInMemory(String ip) {
        long nowMillis = System.currentTimeMillis();
        long windowMillis = TimeUnit.DAYS.toMillis(7);
        String key = KEY_PREFIX + ip;

        LocalRateLimitEntry entry = localCounters.compute(key, (k, current) -> {
            if (current == null || nowMillis >= current.expiresAtMillis) {
                return new LocalRateLimitEntry(1, nowMillis + windowMillis);
            }
            current.count += 1;
            return current;
        });

        return entry.count > limit;
    }

    private static class LocalRateLimitEntry {
        private int count;
        private final long expiresAtMillis;

        private LocalRateLimitEntry(int count, long expiresAtMillis) {
            this.count = count;
            this.expiresAtMillis = expiresAtMillis;
        }
    }
}