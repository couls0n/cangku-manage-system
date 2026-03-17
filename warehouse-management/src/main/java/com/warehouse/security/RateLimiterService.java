package com.warehouse.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    private final Map<String, Deque<Long>> counters = new ConcurrentHashMap<>();

    public boolean allow(String key, int limit, int windowSeconds) {
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
