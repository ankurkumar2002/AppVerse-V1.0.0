package com.appverse.app_service.kafka.events;

import java.time.Instant;

import com.appverse.app_service.enums.ApplicationStatus;



public record ApplicationCreatedEvent(
        String id,

        String developerId,

        String name,

        String categoryId,

        ApplicationStatus status,

        Instant createdAt) {
}
