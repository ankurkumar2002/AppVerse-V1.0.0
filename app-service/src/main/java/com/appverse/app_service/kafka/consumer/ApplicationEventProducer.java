package com.appverse.app_service.kafka.consumer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.appverse.app_service.kafka.events.ApplicationCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventProducer {

    private static final String APPLICATION_CREATED_TOPIC = "application-created";

    private final KafkaTemplate<String, ApplicationCreatedEvent> kafkaTemplate;

    public void publishApplicationCreated(ApplicationCreatedEvent event) {
        log.info("Publishing ApplicationCreatedEvent for applicationId={}", event.id());

        kafkaTemplate.send(
                APPLICATION_CREATED_TOPIC,
                event.id(),
                event).whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Event published successfully. Topic={}, Partition={}, Offset={}",
                                result.getRecordMetadata().topic(), result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish event", ex);
                    }
                });
    }
}
