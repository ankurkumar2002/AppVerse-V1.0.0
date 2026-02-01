package com.appverse.api_gateway.client;


import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.appverse.api_gateway.dto.CachedToken;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServiceTokenProvider {

    private final WebClient webClient;

    @Value("${keycloak.token-uri}")
    private String tokenUri;

    @Value("${appverse.keycloak.client-id}")
    private String clientId;

    @Value("${appverse.keycloak.client-secret}")
    private String clientSecret;

    private final AtomicReference<CachedToken> cachedToken = new AtomicReference<>();

    public String getServiceToken() {

        CachedToken current = cachedToken.get();

        if (current != null && !current.isExpired()) {
            return current.token();
        }

        synchronized (this) {
            current = cachedToken.get();
            if (current != null && !current.isExpired()) {
                return current.token();
            }

            CachedToken newToken = fetchNewToken();
            cachedToken.set(newToken);
            return newToken.token();
        }

    }

    private CachedToken fetchNewToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        JsonNode body = webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (body == null || !body.has("access_token")) {
            throw new IllegalStateException("Failed to obtain service token from keycloak");
        }

        String token = body.get("access_token").asText();
        long expiresIn = body.get("expires_in").asLong();

        return new CachedToken(
                token, Instant.now().plusSeconds(expiresIn));
    }

}
