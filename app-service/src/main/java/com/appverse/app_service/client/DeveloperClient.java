package com.appverse.app_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.observation.annotation.Observed;

@FeignClient(name = "developerClient", url = "http://localhost:8081")
public interface DeveloperClient {

    @RequestMapping(method = RequestMethod.GET, value = "/api/developers/exists")

    @GetExchange("/api/developers/exists")
    @CircuitBreaker(name = "developerClient", fallbackMethod = "isDeveloperByIdFallback")
    @Retry(name = "developerClient")
    @Observed(name = "appService.checkDeveloper", contextualName = "check-developer-existence")
    boolean isDeveloperById(@RequestParam("id") String id);

    default boolean isDeveloperByIdFallback(@RequestParam("id") String id, Throwable throwable) {
        return false; 
    }
}