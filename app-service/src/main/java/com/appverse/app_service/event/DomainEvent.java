package com.appverse.app_service.event;


import java.time.Instant;

public record DomainEvent<T>(
    String eventType,
    String source,
    Instant occurredAt,
    T payload
) {}
