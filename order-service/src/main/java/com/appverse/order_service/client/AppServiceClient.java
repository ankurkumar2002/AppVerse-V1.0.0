package com.appverse.order_service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.appverse.order_service.enums.MonetizationType;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.observation.annotation.Observed;

import java.math.BigDecimal;

// Name should match the application name in Eureka or application.yml for URL config
@FeignClient(name = "app-service" , url = "http://localhost:8080" )
public interface AppServiceClient {

    @GetMapping("/api/apps/{id}") // Assuming this endpoint returns necessary details
    @CircuitBreaker(name = "appServiceClient", fallbackMethod = "getAppDetailsFallback")
    @Retry(name = "appServiceClient")
    @Observed(name = "orderService.getApplication", contextualName = "get-app-details")
    AppDetails getAppDetails(@PathVariable("id") String applicationId);

    // Define a simple DTO for the response from app-service
    // This DTO should match what app-service's GET /api/apps/{id} actually returns
    record AppDetails(
        String id,
        String name,
        String version,
        BigDecimal price,
        String currency,
        MonetizationType monetizationType,
        boolean isFree
        // Add other fields if needed by order service
    ) {}

    default AppDetails getAppDetailsFallback(String applicationId, Throwable throwable) {
        System.out.println("Fallback for getAppDetails: " + throwable.getMessage());
        return new AppDetails(applicationId, "Unknown", "0.0", BigDecimal.ZERO, "USD", null, false);
    }
}
