package com.appvese.notification_service.payload;

public record ApplicationCreatedNotificationPayload(
    String applicationId,
    String applicationName,
    String developerId
) {}
