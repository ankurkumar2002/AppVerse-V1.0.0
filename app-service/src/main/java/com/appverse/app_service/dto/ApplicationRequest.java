package com.appverse.app_service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ApplicationRequest(
    String id,
    String name,
    String tagline,
    String description,
    String version,
    String categoryId, // ID reference
    BigDecimal price,
    String currency,
    boolean isFree,
    List<String> platforms,
    String accessUrl,
    String websiteUrl,
    String supportUrl,
    String thumbnailUrl,
    List<ScreenshotResponse> screenshots, // Use a corresponding record for screenshots
    String developerId, // ID reference
    List<String> tags,
    String status,
    Instant publishedAt,
    Instant createdAt,   // Includes timestamp set by @CreatedDate
    Instant updatedAt,   // Includes timestamp set by @LastModifiedDate
    Double averageRating,
    Integer ratingCount
) {}