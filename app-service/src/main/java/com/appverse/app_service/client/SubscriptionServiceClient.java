package com.appverse.app_service.client; // Ensure this package is exactly correct

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.math.BigDecimal; // Make sure BigDecimal is imported

// Ensure this 'name' matches what you configure for load balancing or direct URL for subscription-service
@FeignClient(name = "subscription-service", url = "${feign.client.subscription-service.url}")
public interface SubscriptionServiceClient {

    // --- Nested Record for the Request Payload ---
    // This record is defined *inside* the SubscriptionServiceClient interface.
    record SubscriptionServicePlanCreationRequest(
        String planNameKey,
        String displayName,
        String description,
        BigDecimal price,
        String currency,
        String billingInterval, // e.g., "MONTH", "YEAR" (sent as String)
        Integer billingIntervalCount,
        Integer trialPeriodDays,
        String applicationId,    // ID of the app in app-service
        String developerId       // ID of the developer
    ) {}

    // --- Nested Record for the Expected Response ---
    // Also defined *inside* the SubscriptionServiceClient interface.
    record SubscriptionServicePlanResponse(
        String id,      // The ID of the plan created in subscription-service
        String name,    // Or displayName, matching what subscription-service returns
        String status   // Example field from subscription-service's plan response
        // Add any other fields that the subscription-service's
        // internal plan creation endpoint actually returns.
    ) {}

    // The Feign client method
    // Ensure this path matches the internal endpoint in subscription-service's SubscriptionPlanController
    @PostMapping("/api/v1/admin/subscription-plans/subscription-plans/by-developer")
    ResponseEntity<SubscriptionServicePlanResponse> createDeveloperSubscriptionPlan(
        @RequestBody SubscriptionServicePlanCreationRequest planRequest
    );
}