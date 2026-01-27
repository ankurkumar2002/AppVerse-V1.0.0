package com.appvese.notification_service.consumer;

import com.appvese.notification_service.event.DomainEvent;
import com.appvese.notification_service.payload.ApplicationCreatedNotificationPayload;
import com.appvese.notification_service.router.NotificationEventRouter;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationEventRouter router;

    @KafkaListener(topics = "application-events", groupId = "notification-service-v4")
    public void consume(String message) {

        try {
            log.info("📩 Raw Kafka message: {}", message);

            // 1️⃣ Parse outer layer
            Object raw = objectMapper.readValue(message, Object.class);

            // 2️⃣ Unwrap if it is a String
            String json = (raw instanceof String)
                    ? (String) raw
                    : message;

            // 3️⃣ Deserialize real JSON
            DomainEvent<ApplicationCreatedNotificationPayload> event = objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory()
                            .constructParametricType(
                                    DomainEvent.class,
                                    ApplicationCreatedNotificationPayload.class));

            log.info("✅ Event deserialized: {}", event.eventType());

            router.route(event);

        } catch (Exception e) {
            log.error("❌ Failed to process Kafka message", e);
            throw new RuntimeException(e);
        }
    }

}
