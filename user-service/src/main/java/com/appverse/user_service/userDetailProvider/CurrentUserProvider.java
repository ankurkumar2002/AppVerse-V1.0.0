package com.appverse.user_service.userDetailProvider;

import org.springframework.stereotype.Component;

import com.appverse.user_service.client.IdentityClient;
import com.appverse.user_service.client.IdentityUserClient;
import com.appverse.user_service.dto.IdentityUserResponse;
import com.appverse.user_service.exception.IntegrationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentUserProvider {

    private final IdentityUserClient identityClient;
    
    public IdentityUserResponse getCurrentUser(){
        try {
            IdentityUserResponse response = identityClient.me();
            log.info(response.id());
            return response;
        } catch (Exception e) {
            throw new IntegrationException("Failed to fetch current user from identity-service");
        }
    }
    
}
