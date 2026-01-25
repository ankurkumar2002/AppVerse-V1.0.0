package com.appvese.notification_service.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.appvese.notification_service.event.DomainEvent;
import com.appvese.notification_service.router.NotificationEventRouter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventConsumer {

    private final NotificationEventRouter router;

    @KafkaListener(
        topics = "application-events",
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(DomainEvent<?> event) throws Exception {
        log.info("Received event {}", event.eventType());
        router.route(event);
    }
}
