package com.appvese.notification_service.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.appvese.notification_service.client.DeveloperServiceClient;
import com.appvese.notification_service.dto.DeveloperEmailResponse;
import com.appvese.notification_service.emailTemplate.EmailTemplateBuilder;
import com.appvese.notification_service.event.DomainEvent;
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
    private final DeveloperServiceClient developerClient;

    public void handleApplicationCreated(
            DomainEvent<ApplicationCreatedNotificationPayload> event) {

        var payload = event.payload();

        // ✅ Fetch email from developer-service (source of truth)
        DeveloperEmailResponse response =
                developerClient.getDeveloperEmail(payload.developerId());

        String developerEmail = response.email();

        sendHtml(
                "rukna2027@gmail.com",
                "Your application is ready",
                templateBuilder.applicationCreated(payload)
        );
    }

    private void sendHtml(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom("noreply@appverse.com");

            mailSender.send(message);

            log.info("📧 Application-created email sent to {}", to);

        } catch (Exception e) {
            log.error("❌ Failed to send email to {}", to, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
