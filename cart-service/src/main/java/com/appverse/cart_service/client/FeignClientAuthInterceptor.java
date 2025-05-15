package com.appverse.cart_service.client; // Or a suitable package like 'interceptor' or 'feign'

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component // <--- This makes it a Spring bean and Feign will auto-discover it
public class FeignClientAuthInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FeignClientAuthInterceptor.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN_TYPE = "Bearer";

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthToken) {
            // Check if the principal is indeed a Jwt object
            if (jwtAuthToken.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                String tokenValue = jwt.getTokenValue();
                log.debug("Feign Interceptor: Adding Bearer token to outgoing request to {}", template.url());
                template.header(AUTHORIZATION_HEADER, String.format("%s %s", BEARER_TOKEN_TYPE, tokenValue));
            } else {
                log.warn("Feign Interceptor: Authentication principal is not a JWT object. Type: {}",
                        jwtAuthToken.getPrincipal() != null ? jwtAuthToken.getPrincipal().getClass().getName() : "null");
            }
        } else if (authentication != null && authentication.isAuthenticated()) {
            log.warn("Feign Interceptor: Authentication is present but not a JwtAuthenticationToken. Type: {}. Token will not be propagated.",
                    authentication.getClass().getName());
            // You might decide to handle other Authentication types here if necessary,
            // but for an OAuth2 Resource Server, JwtAuthenticationToken is standard.
        } else {
            log.debug("Feign Interceptor: No authentication found in SecurityContextHolder or not authenticated. Request to {} will proceed without Authorization header.", template.url());
        }
    }
}