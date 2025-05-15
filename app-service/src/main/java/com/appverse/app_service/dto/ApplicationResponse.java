// === In app-service Project ===
package com.appverse.app_service.dto;

import com.appverse.app_service.enums.MonetizationType; // <<< IMPORT ADDED
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ApplicationResponse(
    String id,
    String name,
    String tagline,
    String description,
    String version,
    String categoryId,
    BigDecimal price,
    String currency,
    boolean isFree,

    // ****************************************************
    // ****** CHANGES START HERE FOR RESPONSE DTO *******
    // ****************************************************
    MonetizationType monetizationType,
    List<String> associatedSubscriptionPlanIds,
    // ****************************************************
    // ****** CHANGES END HERE FOR RESPONSE DTO *********
    // ****************************************************

    List<String> platforms,
    String accessUrl,
    String websiteUrl,
    String supportUrl,
    String thumbnailUrl,
    List<ScreenshotResponse> screenshots,
    String developerId,
    String developerName,
    String categoryName,
    List<String> tags,
    String status,
    Instant publishedAt,
    Instant createdAt,
    Instant updatedAt,
    Double averageRating,
    Integer ratingCount
) {}