package com.appvese.notification_service.emailTemplate;

import org.springframework.stereotype.Component;

import com.appvese.notification_service.payload.ApplicationCreatedNotificationPayload;

@Component
public class EmailTemplateBuilder {

    public String applicationCreated(ApplicationCreatedNotificationPayload payload) {
        return """
            <h2>Application Created 🎉</h2>
            <p>Your application <b>%s</b> has been created successfully.</p>
            <p>Please log in to AppVerse to review and manage it.</p>
            <br/>
            <p>– AppVerse Team</p>
        """.formatted(payload.applicationName());
    }
}
