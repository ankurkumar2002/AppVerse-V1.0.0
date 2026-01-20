package com.appverse.developer_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.appverse.developer_service.dto.IdentityUserResponse;

@FeignClient(
    name = "identity-service-user",
    url = "${services.identity-service.url}",
    configuration = UserFeignAuthConfig.class
)
public interface IdentityUserClient {
    @GetMapping("/api/identity/users/me")
    IdentityUserResponse me();
}
