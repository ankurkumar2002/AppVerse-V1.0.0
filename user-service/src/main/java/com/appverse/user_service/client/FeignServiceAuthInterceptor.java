package com.appverse.user_service.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FeignServiceAuthInterceptor {

    private final ServiceTokenProvider tokenProvider;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return request -> {
            request.header(
                "Authorization",
                "Bearer " + tokenProvider.getServiceToken()
            );
        };
    }
}
