package com.appverse.order_service.service;

import org.springframework.stereotype.Service;

import com.appverse.order_service.client.AppServiceClient;
import com.appverse.order_service.dto.AppDetails;
import com.appverse.order_service.exception.ServiceUnavailableException;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationLookupService {

    private final AppServiceClient client;

    public AppDetails fetch(String appId) {
        try {
            return client.getAppDetails(appId);
        } catch (FeignException e) {
            throw new ServiceUnavailableException(
                "Application service unavailable for appId=" + appId, e
            );
        }
    }
}
