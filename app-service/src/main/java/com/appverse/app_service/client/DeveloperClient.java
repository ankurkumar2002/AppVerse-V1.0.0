package com.appverse.app_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping; // Use GetMapping
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.observation.annotation.Observed;

// Using FeignClient, so we'll use Feign-compatible annotations
@FeignClient(name = "developerClient", url = "http://localhost:8081")
public interface DeveloperClient {

    // Use @GetMapping instead of @GetExchange and remove the redundant @RequestMapping
    @GetMapping("/api/developers/exists")
    boolean isDeveloperById(@RequestParam("id") String id);

    // Correct fallback method: No @RequestParam annotation
    default boolean isDeveloperByIdFallback(String id, Throwable throwable) {
        // Log the error and the ID for which the fallback was triggered
        System.err.println("Fallback for isDeveloperById triggered for id: " + id + ", error: " + throwable.getMessage());
        return false;
    }

    // Use @GetMapping instead of @GetExchange
    @GetMapping("/api/developers/exists/by-keycloak-id/{keycloakUserId}")
    boolean isDeveloperByKeycloakId(@PathVariable("keycloakUserId") String keycloakUserId);

    default boolean isDeveloperByKeycloakIdFallback(String keycloakUserId, Throwable throwable) {
        System.err.println("Fallback for isDeveloperByKeycloakId triggered for keycloakUserId: " + keycloakUserId + ", error: " + throwable.getMessage());
        return false;
    }
}