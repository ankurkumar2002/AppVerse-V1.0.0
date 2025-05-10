package com.appverse.app_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "developer-service", url = "http://localhost:8081")
public interface DeveloperClient {

    @RequestMapping(method = RequestMethod.GET, value = "/api/developers/exists")
    boolean isDeveloperById(@RequestParam("id") String id);
}

