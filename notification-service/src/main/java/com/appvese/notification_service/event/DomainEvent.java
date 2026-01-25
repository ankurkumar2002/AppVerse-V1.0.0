package com.appvese.notification_service.event;

import java.time.Instant;

public record DomainEvent<T>(
    String eventType,      // ORDER_CREATED, APPLICATION_CREATED
    String source,         // order-service, app-service
    Instant occurredAt,
    T payload
) {}
