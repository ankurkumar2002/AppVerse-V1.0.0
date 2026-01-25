package com.appvese.notification_service.emailTemplate;

import org.springframework.stereotype.Component;

@Component
public class ApplicationEmailTemplates {

    public String applicationCreated(String appName) {
        return """
            <h2>Your application is ready 🎉</h2>
            <p><b>%s</b> has been created successfully.</p>
            <p>Please log in to AppVerse to continue.</p>
        """.formatted(appName);
    }

    public String applicationPublished(String appName) {
        return """
            <h2>Your application is live 🚀</h2>
            <p><b>%s</b> is now published and visible to users.</p>
        """.formatted(appName);
    }
}
