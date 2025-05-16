// === In a shared library or app-service's event package ===
package com.appverse.app_service.event; // Or a common event package

import java.time.Instant;
import java.util.UUID;

public record EventMetaData(
    String eventId,     // Unique ID for this specific event instance
    String eventType,   // Type of the event (e.g., "ApplicationCreated", "ApplicationUpdated")
    Instant eventTimestamp, // When the event occurred
    String serviceName, // Name of the service publishing the event (e.g., "app-service")
    String correlationId // Optional: to trace a request across multiple services
) {
    public EventMetaData(String eventType, String serviceName, String correlationId) {
        this(UUID.randomUUID().toString(), eventType, Instant.now(), serviceName, correlationId);
    }
     public EventMetaData(String eventType, String serviceName) {
        this(UUID.randomUUID().toString(), eventType, Instant.now(), serviceName, null);
    }
}