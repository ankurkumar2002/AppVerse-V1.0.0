// === In subscription-service Project ===
package com.appverse.subscription_service.config; // Ensure this package is correct

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    // Define common paths for SpringDoc/Swagger UI
    private static final String[] SWAGGER_UI_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring SecurityFilterChain in subscription-service..."); // UPDATED Log Message

        http
            .addFilterBefore((request, response, chain) -> {
                // UPDATED Log Message
                logger.info("SUBSCRIPTION-SERVICE: Request received at SecurityFilterChain: {} {}",
                        ((jakarta.servlet.http.HttpServletRequest) request).getMethod(),
                        ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI());
                String authHeader = ((jakarta.servlet.http.HttpServletRequest) request).getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    logger.debug("SUBSCRIPTION-SERVICE: Authorization Bearer token present."); // UPDATED Log Message
                } else {
                    String requestURI = ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI();
                    boolean isSwaggerPath = false;
                    for (String path : SWAGGER_UI_PATHS) {
                         if (requestURI.equals(path.replace("/**", "")) || requestURI.startsWith(path.replace("/**", ""))) {
                           if (path.endsWith("/**") && requestURI.startsWith(path.substring(0, path.length() - 3)) ) isSwaggerPath = true;
                           else if (requestURI.equals(path)) isSwaggerPath = true;
                           if(isSwaggerPath) break;
                        }
                    }
                    if (!isSwaggerPath) {
                        // UPDATED Log Message
                        logger.warn("SUBSCRIPTION-SERVICE: No/Invalid Authorization Bearer token found in request to {}", requestURI);
                    }
                }
                chain.doFilter(request, response);
            }, AuthorizationFilter.class)
            .authorizeHttpRequests(authorize -> {
                logger.info("SUBSCRIPTION-SERVICE: Configuring authorizeHttpRequests..."); // UPDATED Log Message
                authorize
                    // ****** ADDED RULE TO PERMIT SWAGGER UI ******
                    .requestMatchers(SWAGGER_UI_PATHS).permitAll()
                    // **********************************************
                    // Secure your subscription service API endpoints
                    .requestMatchers("/api/v1/admin/subscription-plans/**").authenticated() // Admin routes for plans
                    .requestMatchers("/api/v1/users/subscriptions/**").authenticated() // User-specific subscription routes
                    // Add other specific rules for subscription-service if any
                    .anyRequest().authenticated(); // All other requests require authentication
            })
            .oauth2ResourceServer(oauth2 -> {
                logger.info("SUBSCRIPTION-SERVICE: Configuring OAuth2 Resource Server with JWT..."); // UPDATED Log Message
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()));
            })
            .csrf(AbstractHttpConfigurer::disable);

        logger.info("SUBSCRIPTION-SERVICE: SecurityFilterChain configuration complete."); // UPDATED Log Message
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        logger.info("SUBSCRIPTION-SERVICE: Creating JwtAuthenticationConverter with KeycloakRealmRoleConverter."); // UPDATED Log Message
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        // Make sure KeycloakRealmRoleConverter is accessible here
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }
}