package com.appvese.notification_service.payload;

public record ApplicationCreatedNotificationPayload(
    String applicationName,
    String developerEmail
) {}
