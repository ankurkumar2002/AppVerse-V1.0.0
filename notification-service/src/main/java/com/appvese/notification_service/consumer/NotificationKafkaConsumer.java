// package com.appvese.notification_service.consumer;

// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Service;

// import com.appvese.notification_service.created.DomainEvent;
// import com.appvese.notification_service.router.NotificationEventRouter;
// import com.fasterxml.jackson.databind.ObjectMapper;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// @Service
// @RequiredArgsConstructor
// @Slf4j
// public class NotificationKafkaConsumer {

//     private final ObjectMapper objectMapper;
//     private final NotificationEventRouter router;

//     @KafkaListener(
//         topics = "application-events",
//         groupId = "notification-service"
//     )
//     public void listen(String rawMessage) throws Exception {
//         try {
//             DomainEvent<?> event =
//                 objectMapper.readValue(rawMessage, DomainEvent.class);

//             router.route(event);

//         } catch (Exception e) {
//             log.error("Failed to process notification event", e);
//             throw e; 
//         }
//     }
// }
