package com.appverse.order_service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.math.BigDecimal;

// Name should match the application name in Eureka or application.yml for URL config
@FeignClient(name = "app-service" , url = "http://localhost:8080" )
public interface AppServiceClient {

    @GetMapping("/api/apps/{id}") // Assuming this endpoint returns necessary details
    AppDetails getAppDetails(@PathVariable("id") String applicationId);

    // Define a simple DTO for the response from app-service
    // This DTO should match what app-service's GET /api/apps/{id} actually returns
    record AppDetails(
        String id,
        String name,
        String version,
        BigDecimal price,
        String currency,
        boolean isFree
        // Add other fields if needed by order service
    ) {}
}
