package com.appverse.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.appverse.order_service.dto.AppDetails;

@FeignClient(name = "app-service", url = "http://localhost:8080")
public interface AppServiceClient {

    @GetMapping("/api/apps/{id}") 
    AppDetails getAppDetails(@PathVariable("id") String applicationId);
    
}
