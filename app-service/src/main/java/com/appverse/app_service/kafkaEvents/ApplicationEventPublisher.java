package com.appverse.app_service.kafkaEvents;

import java.time.Instant;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.appverse.app_service.event.payload.ApplicationCreatedPayload;
import com.appverse.app_service.event.payload.ApplicationDeletedPayload;
import com.appverse.app_service.event.payload.ApplicationUpdatedPayload;
import com.appverse.app_service.model.Application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventPublisher {
    private static final String APPLICATION_EVENTS_TOPIC = "application-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCreated(Application app) {
        ApplicationCreatedPayload payload = new ApplicationCreatedPayload(app.getId(),
                app.getName(),
                app.getDeveloperId(),
                app.getCategoryId(),
                app.getMonetizationType(),
                app.getPrice(),
                app.getCurrency(),
                app.isFree(),
                app.getPlatforms(),
                app.getStatus(),
                app.getTags(),
                app.getCreatedAt());

        kafkaTemplate.send(APPLICATION_EVENTS_TOPIC, app.getId(), payload);
        log.info("Published ApplicationCreatedEvent for app ID: {}", app.getId());
    }

    public void publishUpdated(Application app) {
        ApplicationUpdatedPayload payload = new ApplicationUpdatedPayload(
                app.getId(),
                app.getName(),
                app.getDeveloperId(),
                app.getCategoryId(),
                app.getMonetizationType(),
                app.getPrice(),
                app.getCurrency(),
                app.isFree(),
                app.getPlatforms(),
                app.getStatus(),
                app.getTags(),
                app.getUpdatedAt());

        kafkaTemplate.send(APPLICATION_EVENTS_TOPIC, app.getId(), payload);
        log.info("Published ApplicationUpdatedEvent for app ID: {}", app.getId());
    }

    public void publishDeleted(Application app) {
        ApplicationDeletedPayload payload = new ApplicationDeletedPayload(
                app.getId(),
                app.getDeveloperId(),
                app.getName(),
                Instant.now());

        kafkaTemplate.send(APPLICATION_EVENTS_TOPIC, app.getId(), payload);
        log.info("Published ApplicationDeletedEvent for app ID: {}", app.getId());
    }
}
