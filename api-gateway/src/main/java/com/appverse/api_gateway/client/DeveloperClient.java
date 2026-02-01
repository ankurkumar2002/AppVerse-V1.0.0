package com.appverse.api_gateway.client;

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

    @GetMapping("/api/developers/exists")
    boolean isDeveloperById(@RequestParam("id") String id);

    

    @GetMapping("/api/developers/exists/by-keycloak-id/{keycloakUserId}")
    boolean isDeveloperByKeycloakId(@PathVariable("keycloakUserId") String keycloakUserId);

    
}