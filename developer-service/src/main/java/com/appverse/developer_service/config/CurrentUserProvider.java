package com.appverse.developer_service.config;

import org.hibernate.boot.beanvalidation.IntegrationException;
import org.springframework.stereotype.Component;

import com.appverse.developer_service.client.IdentityUserClient;
import com.appverse.developer_service.dto.IdentityUserResponse;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentUserProvider {

    private final IdentityUserClient identityUserClient;

    public IdentityUserResponse getCurrentUser(){
        try {
            IdentityUserResponse rsponse = identityUserClient.me();
            log.info(rsponse.firstName()+" and "+ rsponse.lastName());
            return rsponse;
        } catch (FeignException e) {
            throw new IntegrationException("Failed to fetch current user from identity-service", e);
        }
    }
}
