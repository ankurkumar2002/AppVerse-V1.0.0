package com.appverse.app_service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

// DTO representing the application data sent back from the API
public record ApplicationResponse(
    String id, // Include ID in responses
    String name,
    String tagline,
    String description,
    String version,
    String categoryId, // Send back the ID reference
    // Or potentially: CategoryResponse category, // If you fetch and map the full object in service
    BigDecimal price,
    String currency,
    boolean isFree,
    List<String> platforms,
    String accessUrl,
    String websiteUrl,
    String supportUrl,
    String thumbnailUrl,
    List<ScreenshotResponse> screenshots, // Use the specific response DTO for screenshots
    Long developerId, // Send back the ID reference
    // Or potentially: DeveloperResponse developer, // If you fetch and map the full object in service
    String developerName,
    String categoryName, // Include category name for better readability
    List<String> tags,
    String status,
    Instant publishedAt,
    Instant createdAt,   // Include timestamp from @CreatedDate
    Instant updatedAt,   // Include timestamp from @LastModifiedDate
    Double averageRating, // Include calculated/stored rating
    Integer ratingCount  // Include calculated/stored count
) {}