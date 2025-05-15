// === In app-service Project ===
package com.appverse.app_service.dto;

import com.appverse.app_service.enums.MonetizationType;
import jakarta.validation.Valid; // For validating nested DTOs
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ApplicationRequest(
    String id,
    String name,
    String tagline,
    String description,
    String version,
    String categoryId,
    BigDecimal price, // Price for ONE_TIME_PURCHASE
    String currency,
    boolean isFree,
    @NotNull MonetizationType monetizationType,

    // ****** MODIFIED/ADDED FOR DEVELOPER-DEFINED PLANS ******
    List<@Valid DeveloperOfferedSubscriptionPlanDto> offeredSubscriptionPlans, // List of plans developer wants for this app
    // 'associatedSubscriptionPlanIds' might now be less relevant if plans are created WITH the app
    // List<String> associatedSubscriptionPlanIds, // Keep if you also link to existing platform-wide plans
    // *********************************************************

    List<String> platforms,
    String accessUrl,
    String websiteUrl,
    String supportUrl,
    String developerId,
    List<String> tags,
    String status,
    Instant publishedAt
) {}