// === In app-service Project ===
package com.appverse.app_service.dto;

import com.appverse.app_service.enums.ApplicationStatus;
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
    BigDecimal price,
    String currency,
    boolean isFree,
    @NotNull MonetizationType monetizationType,
    List<@Valid DeveloperOfferedSubscriptionPlanDto> offeredSubscriptionPlans,
    List<String> platforms,
    String accessUrl,
    String websiteUrl,
    String supportUrl,
    List<String> tags
) {}