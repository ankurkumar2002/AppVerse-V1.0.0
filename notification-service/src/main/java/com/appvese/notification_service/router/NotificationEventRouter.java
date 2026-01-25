package com.appvese.notification_service.router;

import org.springframework.stereotype.Service;

import com.appvese.notification_service.event.DomainEvent;
import com.appvese.notification_service.payload.ApplicationCreatedNotificationPayload;
import com.appvese.notification_service.service.EmailNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventRouter {

    private final EmailNotificationService emailService;

    public void route(DomainEvent<?> event) throws Exception {

        switch (event.eventType()) {

            case "APPLICATION_CREATED" ->
                emailService.handleApplicationCreated(
                    cast(event, ApplicationCreatedNotificationPayload.class)
                );

            default ->
                log.info("No notification configured for event {}", event.eventType());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> DomainEvent<T> cast(DomainEvent<?> event, Class<T> type) {
        return (DomainEvent<T>) event;
    }
}
