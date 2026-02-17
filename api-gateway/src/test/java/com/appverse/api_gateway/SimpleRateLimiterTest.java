package com.appverse.api_gateway;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.appverse.api_gateway.config.SimpleRateLimiter;

public class SimpleRateLimiterTest {
    private SimpleRateLimiter rateLimiter;

    @BeforeEach
    void setup() {
        rateLimiter = new SimpleRateLimiter();
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        String key = "token";

        for (int i = 0; i < 100; i++) {
            assertTrue(rateLimiter.allow(key));
        }
    }

    @Test
    void shouldBlockAfterLimitExceeded(){
        String key = "token";

        for (int i = 0; i < 100; i++) {
            rateLimiter.allow(key);
        }

        assertFalse(rateLimiter.allow(key));
    }

    @Test
    void shouldRejectNullKey() {
        assertFalse(rateLimiter.allow(null));
    }
}
