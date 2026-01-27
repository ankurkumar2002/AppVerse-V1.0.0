package com.appverse.app_service.kafkaEvents;

import java.time.Instant;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.appverse.app_service.event.DomainEvent;
import com.appverse.app_service.event.payload.ApplicationCreatedNotificationPayload;
import com.appverse.app_service.event.payload.ApplicationDeletedPayload;
import com.appverse.app_service.event.payload.ApplicationUpdatedPayload;
import com.appverse.app_service.model.Application;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventPublisher {

    private static final String TOPIC = "application-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishCreated(Application app) {
        try {
            ApplicationCreatedNotificationPayload payload = new ApplicationCreatedNotificationPayload(
                    app.getId(),
                    app.getName(),
                    app.getDeveloperId()
            );

            DomainEvent<ApplicationCreatedNotificationPayload> event =
                    new DomainEvent<>(
                            "APPLICATION_CREATED",
                            "app-service",
                            Instant.now(),
                            payload
                    );

            String json = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(TOPIC, app.getId(), json);

            log.info("Published APPLICATION_CREATED event for app {}", app.getId());

        } catch (Exception e) {
            log.error("Failed to publish application created event", e);
            throw new RuntimeException(e);
        }
    }
}
