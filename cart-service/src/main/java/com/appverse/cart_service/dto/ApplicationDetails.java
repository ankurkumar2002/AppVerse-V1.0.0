package com.appverse.cart_service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ApplicationDetails(
        String id,
        String name,
        String tagline,
        String description,
        String version,
        String categoryId,
        BigDecimal price, 
        String currency,
        boolean isFree,   
        List<String> platforms,
        String accessUrl,
        String websiteUrl,
        String supportUrl,
        String thumbnailUrl,
        List<ScreenshotDetails> screenshots, 
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