package com.appvese.notification_service.kafka.events;

import java.time.Instant;

import com.appvese.notification_service.enums.ApplicationStatus;

public record ApplicationCreatedEvent(
        String id,

        String developerId,

        String name,

        String categoryId,

        ApplicationStatus status,

        Instant createdAt) {
}
