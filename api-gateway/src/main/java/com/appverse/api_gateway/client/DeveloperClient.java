package com.appverse.api_gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping; // Use GetMapping
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "developerClient", url = "http://localhost:8081")
public interface DeveloperClient {

    @GetMapping("/api/developers/exists/by-keycloak-id/{keycloakUserId}")
    ResponseEntity<Boolean> isDeveloperByKeycloakId(
        @PathVariable String keycloakUserId
    );
}
