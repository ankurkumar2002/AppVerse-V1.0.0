package com.appverse.api_gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "userClient", url = "http://localhost:8082")
public class UserClient {
    // @GetMapping("/api/v1/users/exists")
    // public ResponseEntity<>
}
