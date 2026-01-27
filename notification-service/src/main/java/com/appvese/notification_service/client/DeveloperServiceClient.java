// === In Subscription Service Project ===
package com.appvese.notification_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.appvese.notification_service.dto.DeveloperEmailResponse;

@FeignClient(name = "developer-service", url = "http://localhost:8081")
public interface DeveloperServiceClient {

    @GetMapping("/api/developers/internal/{developerId}/email")
    DeveloperEmailResponse getDeveloperEmail(
            @PathVariable("developerId") String developerId);

}