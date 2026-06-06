package com.appverse.user_service.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FeignServiceAuthInterceptor {

    private final ServiceTokenProvider tokenProvider;

    @PostConstruct
    public void init() {
        log.info("INTERCEPTOR CONFIG LOADED");
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return request -> {
            String token = tokenProvider.getServiceToken();
            System.out.println("SERVICE TOKEN INTERCEPTOR EXECUTED");
            log.info(token);
            request.header(
                    "Authorization",
                    "Bearer " + token);
        };
    }
}
