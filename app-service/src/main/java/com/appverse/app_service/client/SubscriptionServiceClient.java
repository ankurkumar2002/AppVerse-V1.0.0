package com.appverse.app_service.client; // Ensure this package is exactly correct

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.observation.annotation.Observed;

import java.math.BigDecimal; // Make sure BigDecimal is imported

// Ensure this 'name' matches what you configure for load balancing or direct URL for subscription-service
@FeignClient(name = "subscription-service", url = "${feign.client.subscription-service.url}")
public interface SubscriptionServiceClient {


    record SubscriptionServicePlanCreationRequest(
        String planNameKey,
        String displayName,
        String description,
        BigDecimal price,
        String currency,
        String billingInterval,
        Integer billingIntervalCount,
        Integer trialPeriodDays,
        String applicationId,    
        String developerId       
    ) {}

   
    record SubscriptionServicePlanResponse(
        String id,   
        String name,    
        String status   
    ) {}


    @PostMapping("/api/v1/admin/subscription-plans/subscription-plans/by-developer")
    @CircuitBreaker(name = "subscriptionServiceClient", fallbackMethod = "createDeveloperSubscriptionPlanFallback")
    @Retry(name = "subscriptionServiceClient")
    @Observed(name = "appService.createSubscriptionPlan", contextualName = "create-developer-subscription-plan")
    ResponseEntity<SubscriptionServicePlanResponse> createDeveloperSubscriptionPlan(
        @RequestBody SubscriptionServicePlanCreationRequest planRequest
    );


    default ResponseEntity<SubscriptionServicePlanResponse> createDeveloperSubscriptionPlanFallback(
        SubscriptionServicePlanCreationRequest planRequest, Throwable throwable) {
        return ResponseEntity
                .status(503)
                .body(new SubscriptionServicePlanResponse(
                        "fallback-id", "Fallback Plan", "FAILED"
                ));
    }
}