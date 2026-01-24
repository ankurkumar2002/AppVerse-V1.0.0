package com.appverse.api_gateway.config;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class SimpleRateLimiter {

    private static final int LIMIT = 100;
    private static final long WINDOW_SECONDS = 60;
    private final Map<String, Window> store = new ConcurrentHashMap<>();

    public boolean allow(String key) {
        if (key == null) return false;

        long now = Instant.now().getEpochSecond();

        return store.compute(key, (k, window) -> {
            if(window == null || now - window.start >= WINDOW_SECONDS) {
                return new Window(now, 1);
            }
            window.count++;
            return window;
        }).count <= LIMIT;
    }

    private static class Window {
        long start;
        int count;

        Window(long start, int count) {
            this.start = start;
            this.count = count;
        }
    }



}