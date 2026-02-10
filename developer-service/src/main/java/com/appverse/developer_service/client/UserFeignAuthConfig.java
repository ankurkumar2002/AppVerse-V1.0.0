package com.appverse.developer_service.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class UserFeignAuthConfig {
    
    @Bean
    public RequestInterceptor userTokenInterceptor() {
        return (RequestTemplate template) ->{
            var auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("AUTH IN FEIGN: "+auth);

            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                log.info(jwt.getTokenValue());
                template.header("Authorization", "Bearer "+jwt.getTokenValue());
            }
        };
    }
}
