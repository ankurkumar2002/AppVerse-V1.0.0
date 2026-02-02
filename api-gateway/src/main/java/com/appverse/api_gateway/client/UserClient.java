package com.appverse.api_gateway.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "userClient", url = "http://localhost:8082")
public interface UserClient {
    @GetMapping("/api/v1/users/exists")
    Map<String, Boolean> checkUserExists();
}
