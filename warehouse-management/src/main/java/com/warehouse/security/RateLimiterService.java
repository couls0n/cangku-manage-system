package com.warehouse.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final String RATE_LIMIT_KEY_PREFIX = "rate-limit:";

    private final StringRedisTemplate stringRedisTemplate;
    private final Map<String, Deque<Long>> counters = new ConcurrentHashMap<>();

    public RateLimiterService(ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider) {
        this.stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
    }

    public boolean allow(String key, int limit, int windowSeconds) {
        if (stringRedisTemplate != null) {
            return allowWithRedis(key, limit, windowSeconds);
        }
        return allowLocally(key, limit, windowSeconds);
    }

    private boolean allowWithRedis(String key, int limit, int windowSeconds) {
        try {
            String redisKey = RATE_LIMIT_KEY_PREFIX + key;
            Long current = stringRedisTemplate.opsForValue().increment(redisKey);
            if (current == null) {
                return allowLocally(key, limit, windowSeconds);
            }
            if (current == 1L) {
                stringRedisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
            }
            return current <= limit;
        } catch (RedisConnectionFailureException ex) {
            return allowLocally(key, limit, windowSeconds);
        }
    }

    private boolean allowLocally(String key, int limit, int windowSeconds) {
        Deque<Long> hits = counters.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        long now = Instant.now().getEpochSecond();
        long threshold = now - windowSeconds;
        synchronized (hits) {
            while (!hits.isEmpty() && hits.peekFirst() <= threshold) {
                hits.pollFirst();
            }
            if (hits.size() >= limit) {
                return false;
            }
            hits.addLast(now);
            return true;
        }
    }
}
