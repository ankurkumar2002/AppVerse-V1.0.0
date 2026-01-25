package com.appvese.notification_service.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.appvese.notification_service.event.DomainEvent;
import com.appvese.notification_service.emailTemplate.EmailTemplateBuilder;
import com.appvese.notification_service.payload.ApplicationCreatedNotificationPayload;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final EmailTemplateBuilder templateBuilder;

    public void handleApplicationCreated(
        DomainEvent<ApplicationCreatedNotificationPayload> event) throws Exception {

        var payload = event.payload();

        sendHtml(
            payload.developerEmail(),
            "Your application is ready",
            templateBuilder.applicationCreated(payload)
        );
    }

    private void sendHtml(String to, String subject, String body) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        helper.setFrom("noreply@appverse.com");

        mailSender.send(message);
        log.info("Email sent to {}", to);
    }
}
