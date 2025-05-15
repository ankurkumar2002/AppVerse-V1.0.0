package com.appverse.subscription_service.config;

import org.slf4j.Logger; // Import SLF4J Logger
import org.slf4j.LoggerFactory; // Import SLF4J LoggerFactory
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // For CSRF
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter; // Example for adding a custom filter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig {

    // Define a logger for this class
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring SecurityFilterChain in app-service...");

        http
            // Optional: Add a custom filter for logging before Spring Security's main processing
            // This filter will execute early in the chain.
            .addFilterBefore((request, response, chain) -> {
                logger.info("APP-SERVICE: Request received at SecurityFilterChain: {} {}",
                        ((jakarta.servlet.http.HttpServletRequest) request).getMethod(),
                        ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI());
                // You can inspect headers here too if needed, e.g., Authorization header
                String authHeader = ((jakarta.servlet.http.HttpServletRequest) request).getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    logger.debug("APP-SERVICE: Authorization Bearer token present.");
                } else {
                    logger.warn("APP-SERVICE: No/Invalid Authorization Bearer token found in request to {}", ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI());
                }
                chain.doFilter(request, response);
            }, AuthorizationFilter.class) // Add before AuthorizationFilter or another suitable Spring Security filter
            .authorizeHttpRequests(authorize -> {
                logger.info("APP-SERVICE: Configuring authorizeHttpRequests...");
                authorize
                    .requestMatchers("/api/apps/test-public").permitAll() // Example of a public endpoint
                    .anyRequest().authenticated(); // All other requests require authentication
            })
            .oauth2ResourceServer(oauth2 -> {
                logger.info("APP-SERVICE: Configuring OAuth2 Resource Server with JWT...");
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()));
            })
            .csrf(AbstractHttpConfigurer::disable); // Disable CSRF for stateless APIs

        // Optional: After security processing, log authentication details
        // This is a bit trickier to do directly in the chain configuration like this for every request.
        // A better place for detailed auth logging AFTER successful authentication is often in a controller
        // or a later filter. However, you can add a general log here.
        // For more detailed post-authentication logging, consider AOP or a custom authentication success handler.

        logger.info("APP-SERVICE: SecurityFilterChain configuration complete.");
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        logger.info("APP-SERVICE: Creating JwtAuthenticationConverter with KeycloakRealmRoleConverter.");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    // You could also add a separate bean that logs authentication after it's set
    // This is just an illustrative example of how you *could* try to log it,
    // but it's not a standard filter. For detailed auth logging after success,
    // consider aspects or custom success handlers.
    /*
    @Bean
    public ApplicationListener<AuthenticationSuccessEvent> authenticationSuccessEvent() {
        return event -> {
            logger.info("APP-SERVICE: Authentication successful for: {}", event.getAuthentication().getName());
            logger.info("APP-SERVICE: Authorities: {}", event.getAuthentication().getAuthorities());
        };
    }
    */
}