package com.appverse.cart_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.observation.annotation.Observed;

import java.math.BigDecimal; // Import BigDecimal
import java.time.Instant;    // Import Instant
import java.util.List;       // Import List

@FeignClient(name = "application-service", url = "${appverse.application-service.url}")
public interface ApplicationServiceClient {

    // Define a record for Screenshot if ApplicationDetails will include it
    // This MUST match the structure of ScreenshotResponse from app-service
    record ScreenshotDetails( // Assuming ScreenshotResponse has these fields
        String id,
        String imageUrl,
        String caption,
        Integer order // or int
    ) {}

    // This record now mirrors ApplicationResponse from application-service
    record ApplicationDetails(
        String id,
        String name,
        String tagline,
        String description,
        String version,
        String categoryId,
        BigDecimal price, // Use java.math.BigDecimal
        String currency,
        boolean isFree,   // Jackson should handle mapping JSON "free" or "isFree" to this
        List<String> platforms,
        String accessUrl,
        String websiteUrl,
        String supportUrl,
        String thumbnailUrl,
        List<ScreenshotDetails> screenshots, // Use the ScreenshotDetails record defined above
        String developerId,           // Matches Long type from ApplicationResponse
        String developerName,
        String categoryName,
        List<String> tags,
        String status,
        Instant publishedAt,        // Use java.time.Instant
        Instant createdAt,
        Instant updatedAt,
        Double averageRating,       // Use Double (object type) for nullability
        Integer ratingCount         // Use Integer (object type) for nullability
    ) {}

    // @GetMapping("/api/apps/{applicationId}")
    @GetMapping("/api/apps/{applicationId}")
    @CircuitBreaker(name = "applicationServiceClient", fallbackMethod = "getApplicationDetailsFallback")
    @Retry(name = "applicationServiceClient")
    @Observed(name = "cartService.GetApplication", contextualName = "get-application-details")
    ApplicationDetails getApplicationDetails(@PathVariable("applicationId") String applicationId);

    default ApplicationDetails getApplicationDetailsFallback(String applicationId, Throwable throwable) {
        // Return a default or empty ApplicationDetails object
        return new ApplicationDetails(
                applicationId,
                "Fallback App",
                "Fallback Tagline",
                "Fallback Description",
                "1.0",
                "fallback-category-id",
                BigDecimal.ZERO,
                "USD",
                false,
                List.of(),
                "http://fallback-url.com",
                "http://fallback-website.com",
                "http://fallback-support.com",
                "http://fallback-thumbnail.com",
                List.of(new ScreenshotDetails("fallback-screenshot-id", "http://fallback-screenshot.com", "Fallback Caption", 1)),
                "fallback-developer-id",
                "Fallback Developer Name",
                "Fallback Category Name",
                List.of(),
                "inactive",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                0.0,
                0
        );
    }
}