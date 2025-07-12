// === In a shared library or app-service's event package ===
package com.appverse.app_service.event;

import java.time.Instant;
import java.util.UUID;

public record EventMetaData(
    String eventId,
    String eventType,
    Instant eventTimestamp,
    String serviceName,
    String correlationId
) {
    public EventMetaData(String eventType, String serviceName, String correlationId) {
        this(UUID.randomUUID().toString(), eventType, Instant.now(), serviceName, correlationId);
    }
    public EventMetaData(String eventType, String serviceName) {
        this(UUID.randomUUID().toString(), eventType, Instant.now(), serviceName, null);
    }
}