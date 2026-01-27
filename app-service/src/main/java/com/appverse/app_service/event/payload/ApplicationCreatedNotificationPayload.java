// === In app-service Project ===
package com.appverse.app_service.event.payload;

import com.appverse.app_service.enums.MonetizationType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ApplicationCreatedNotificationPayload(
    String applicationId,
    String applicationName,
    String developerId
) {}
