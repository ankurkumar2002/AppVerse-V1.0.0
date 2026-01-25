package com.appverse.cart_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.appverse.cart_service.dto.ApplicationDetails;

@FeignClient(name = "application-service", url = "${appverse.application-service.url}")
public interface ApplicationServiceClient {

    @GetMapping("/api/apps/{applicationId}")
    ApplicationDetails getApplicationDetails(@PathVariable("applicationId") String applicationId);

}