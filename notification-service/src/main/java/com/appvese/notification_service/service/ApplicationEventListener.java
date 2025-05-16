// === In notification-service Project ===
package com.appvese.notification_service.service; // Renamed package for clarity


import com.appvese.notification_service.payload.ApplicationCreatedNotificationPayload;
import com.fasterxml.jackson.core.JsonProcessingException;

// import com.appverse.notification_service.service.EmailService; // If you have a dedicated EmailService

import com.fasterxml.jackson.databind.ObjectMapper; // For JSON deserialization
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage; // For simple emails
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper; // For HTML emails
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventListener { // Renamed class for better context

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper; // For deserializing JSON string to DTO

    // Assuming app-service publishes to "application-events" topic
    // and the payload is a JSON string of ApplicationCreatedPayload
    @KafkaListener(
            topics = "application-events", // Topic where app-service publishes ApplicationCreatedEvent
            groupId = "notification-group-app-created", // Consumer group ID
            // You can configure a KafkaListenerContainerFactory if you need specific deserializers
            // for keys/values directly at the listener level, or rely on global config.
            // For JSON string payloads, the default StringDeserializer for value is fine.
            // We then manually parse it.
            containerFactory = "kafkaListenerContainerFactory" // Assuming you have a default factory
    )
    public void handleApplicationCreatedEvent(String messageJson) {
        log.info("Received raw application created event message: {}", messageJson);
        try {
            // Deserialize the JSON string message into our DTO
            ApplicationCreatedNotificationPayload payload = objectMapper.readValue(messageJson, ApplicationCreatedNotificationPayload.class);
            log.info("Deserialized payload for new application: ID={}, Name='{}'", payload.id(), payload.name());

            // --- Prepare and Send Email ---
            // You'd likely fetch the developer's email address here using payload.developerId()
            // by calling a user-service or developer-service (via Feign client).
            // For this example, we'll use a placeholder email.
            String recipientEmail = "ankurkumarsingh0488@gmail.com"; // Placeholder
            String subject = "New Application Created: " + payload.name();
            String emailBody = buildApplicationCreatedEmailBody(payload);

            sendEmail(recipientEmail, subject, emailBody, true); // Send as HTML

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize application created event JSON: {}", messageJson, e);
            // Potentially send to a dead-letter topic (DLT) or handle error
        } catch (MailException e) {
            log.error("Failed to send application creation notification email for app name '{}': {}",
                    (messageJson.contains("\"name\"") ? "parsed_name_placeholder" : "unknown_name"), // Basic name extraction for log
                    e.getMessage(), e);
            // Handle email sending failure (e.g., retry, log for manual follow-up)
        } catch (Exception e) { // Catch-all for other unexpected errors
            log.error("An unexpected error occurred while processing application created event: {}", messageJson, e);
        }
    }

    private String buildApplicationCreatedEmailBody(ApplicationCreatedNotificationPayload payload) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>New Application Created!</h1>");
        sb.append("<p>Hello Developer,</p>"); // This should ideally be personalized
        sb.append("<p>A new application has been successfully submitted to AppVerse:</p>");
        sb.append("<ul>");
        sb.append("<li><b>Application Name:</b> ").append(payload.name() != null ? payload.name() : "N/A").append("</li>");
        sb.append("<li><b>Application ID:</b> ").append(payload.id() != null ? payload.id() : "N/A").append("</li>");

        // Handle price and currency carefully if they can be null
        String priceString = "N/A";
        if (payload.price() != null && payload.currency() != null) {
            priceString = payload.price().toString() + " " + payload.currency();
        } else if (payload.price() != null) {
            priceString = payload.price().toString();
        }
        sb.append("<li><b>Price:</b> ").append(priceString).append(" (Free: ").append(payload.isFree()).append(")</li>");

        if (payload.createdAt() != null) {
            sb.append("<li><b>Created At:</b> ").append(payload.createdAt().toString()).append("</li>");
        } else {
            sb.append("<li><b>Created At:</b> Not available</li>");
        }
        // The duplicate line that was here has been removed.
        sb.append("</ul>");
        sb.append("<p>You can view it in your developer portal.</p>");
        sb.append("<p>Thank you,<br/>The AppVerse Team</p>");
        return sb.toString();
    }

    private void sendEmail(String to, String subject, String body, boolean isHtml) {
        log.info("Attempting to send email to: {}, Subject: {}", to, subject);
        try {
            if (isHtml) {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
                helper.setText(body, true); // true = isHtml
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setFrom("noreply@appverse.com"); // Set your from address
                mailSender.send(mimeMessage);
            } else {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                message.setFrom("noreply@appverse.com"); // Set your from address
                mailSender.send(message);
            }
            log.info("Email sent successfully to: {}", to);
        } catch (MailException e) { // Catch MailException for SimpleMailMessage
            log.error("Error sending email to {}: {}", to, e.getMessage(), e);
            throw e; // Re-throw to be caught by the listener's catch block if needed
        } catch (MessagingException e) { // Catch MessagingException for MimeMessage
            log.error("Error sending email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException(e); // Wrap in unchecked exception
        }
    }

    // You could add other @KafkaListener methods here for other event types
    // like ApplicationUpdatedEvent, ApplicationDeletedEvent, etc., each consuming
    // from the same topic but deserializing into their specific payload DTOs
    // or consuming from different topics if you choose that strategy.
}