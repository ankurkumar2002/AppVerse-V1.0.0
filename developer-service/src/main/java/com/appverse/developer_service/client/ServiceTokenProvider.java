package com.appverse.developer_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

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

    public String getServiceToken() {
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
        
        return (body != null && body.has("access_token")) ? body.get("access_token").asText(): null;
    }
    
}
