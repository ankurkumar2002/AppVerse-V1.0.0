package com.appvese.notification_service.dto;


import java.time.Instant;

public record CachedToken(
        String token,
        Instant expiresAt
) {
    public boolean isExpired() {
        // refresh 30 seconds before expiry
        return Instant.now().isAfter(expiresAt.minusSeconds(30));
    }
}
