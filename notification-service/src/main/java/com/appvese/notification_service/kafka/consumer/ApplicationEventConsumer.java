package com.appvese.notification_service.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.appvese.notification_service.kafka.events.ApplicationCreatedEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApplicationEventConsumer {

    @KafkaListener(topics = "application-created", groupId = "app-service-group")
    public void consume(ApplicationCreatedEvent event) {
        log.info("======================================");
        log.info("APPLICATION CREATED EVENT RECEIVED");
        log.info("Application Id : {}", event.id());
        log.info("Developer Id   : {}", event.developerId());
        log.info("Application    : {}", event.name());
        log.info("Status         : {}", event.status());
        log.info("======================================");
    }
}
