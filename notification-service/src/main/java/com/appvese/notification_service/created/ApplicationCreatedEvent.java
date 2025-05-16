package com.appvese.notification_service.created;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationCreatedEvent {

    private final JavaMailSender mailSender;
    
    @KafkaListener(topics = "app-service")
    public void listen(String message) {
        log.info("Received notification: {}", message);
    
    }
}
