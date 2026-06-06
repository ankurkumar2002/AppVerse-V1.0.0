package com.appverse.user_service.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;


import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class UserFeignAuthConfig {
    
    @Bean
    public RequestInterceptor userTokenInterceptor(){
        return (RequestTemplate template) -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("AUTH IN FEIGN: " + auth);
            System.out.println("USER TOKEN INTERCEPTOR EXECUTED");

            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                template.header("Authorization", "Bearer "+jwt.getTokenValue());
            }
        };
    }
}
