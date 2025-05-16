package com.appverse.app_service.config;

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

    // ****** ADDED SWAGGER UI PATHS ******
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
    // ************************************

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring SecurityFilterChain in app-service...");

        http
            .addFilterBefore((request, response, chain) -> {
                logger.info("APP-SERVICE: Request received at SecurityFilterChain: {} {}",
                        ((jakarta.servlet.http.HttpServletRequest) request).getMethod(),
                        ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI());
                String authHeader = ((jakarta.servlet.http.HttpServletRequest) request).getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    logger.debug("APP-SERVICE: Authorization Bearer token present.");
                } else {
                    // Log only if the path is NOT a Swagger path, to reduce noise for permitted paths
                    String requestURI = ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI();
                    boolean isSwaggerPath = false;
                    for (String path : SWAGGER_UI_PATHS) {
                        // Basic matching, AntPathMatcher would be more robust if paths have wildcards like /api/**
                        if (requestURI.equals(path.replace("/**", "")) || requestURI.startsWith(path.replace("/**", ""))) {
                           if (path.endsWith("/**") && requestURI.startsWith(path.substring(0, path.length() - 3)) ) isSwaggerPath = true;
                           else if (requestURI.equals(path)) isSwaggerPath = true;
                           if(isSwaggerPath) break;
                        }
                    }
                    if (!isSwaggerPath) {
                         logger.warn("APP-SERVICE: No/Invalid Authorization Bearer token found in request to {}", requestURI);
                    }
                }
                chain.doFilter(request, response);
            }, AuthorizationFilter.class)
            .authorizeHttpRequests(authorize -> {
                logger.info("APP-SERVICE: Configuring authorizeHttpRequests...");
                authorize
                    // ****** ADDED RULE TO PERMIT SWAGGER UI ******
                    .requestMatchers(SWAGGER_UI_PATHS).permitAll()
                    // **********************************************
                    .requestMatchers("/api/apps/test-public").permitAll()
                    .requestMatchers("/api/apps/**").authenticated() // Explicitly secure your API endpoints
                    .requestMatchers("/actuator/**").permitAll() // Actuator endpoints are public
                    .anyRequest().authenticated(); // All other requests require authentication
            })
            .oauth2ResourceServer(oauth2 -> {
                logger.info("APP-SERVICE: Configuring OAuth2 Resource Server with JWT...");
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()));
            })
            .csrf(AbstractHttpConfigurer::disable);

        logger.info("APP-SERVICE: SecurityFilterChain configuration complete.");
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        logger.info("APP-SERVICE: Creating JwtAuthenticationConverter with KeycloakRealmRoleConverter.");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }
}