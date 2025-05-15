package com.appverse.order_service.client;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component // Register this as a Spring Bean
public class FeignClientInterceptor implements RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(FeignClientInterceptor.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN_TYPE = "Bearer";

    @Override
    public void apply(RequestTemplate template) {
        // Check if the header already exists (e.g., set manually elsewhere)
        if (template.headers().containsKey(AUTHORIZATION_HEADER)) {
             logger.debug("Authorization header already exists for Feign request to {}. Skipping.", template.url());
             return;
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String tokenValue = jwtAuth.getToken().getTokenValue();
            if (tokenValue != null) {
                logger.debug("Adding Authorization header to Feign request to {}", template.url());
                template.header(AUTHORIZATION_HEADER, String.format("%s %s", BEARER_TOKEN_TYPE, tokenValue));
            } else {
                 logger.warn("JWT token value is null, cannot add Authorization header for Feign request to {}", template.url());
            }
        } else {
            // Log if authentication is not the expected type or is null
            if (authentication != null) {
                logger.warn("Current authentication type ({}) is not JwtAuthenticationToken. Cannot propagate token for Feign request to {}",
                        authentication.getClass().getSimpleName(), template.url());
            } else {
                 logger.warn("No authentication found in SecurityContextHolder. Cannot add Authorization header for Feign request to {}", template.url());
            }
        }
    }
}