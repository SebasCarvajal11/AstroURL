package com.astrourl.api.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimitServiceTest {

    @Test
    void redisThrowsCheckedExceptionFallsBackToInMemory() throws Exception {
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        doAnswer(inv -> {
            throw new Exception("simulado");
        }).when(ops).get(anyString());

        StringRedisTemplate template = mock(StringRedisTemplate.class);
        when(template.opsForValue()).thenReturn(ops);

        RateLimitService service = new RateLimitService(template, 10);
        assertFalse(service.isLimitExceeded("203.0.113.1"));
    }
}
