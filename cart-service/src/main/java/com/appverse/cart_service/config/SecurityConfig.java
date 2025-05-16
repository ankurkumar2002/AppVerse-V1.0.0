// === In cart-service Project ===
package com.appverse.cart_service.config; // Ensure this package is correct

import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.Customizer; // Keep if you might use .jwt(Customizer.withDefaults())
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class); // Added logger

    // Paths for Swagger UI
    private static final String[] SWAGGER_UI_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/webjars/**"
    };

    // Paths for public Actuator endpoints
    private static final String[] PUBLIC_ACTUATOR_PATHS = {
            "/actuator/prometheus",
            "/actuator/health" // It's common to expose health endpoint too
            // Add any other actuator endpoints you want public
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring SecurityFilterChain in cart-service...");
        http
            .authorizeHttpRequests(authorize -> {
                logger.info("CART-SERVICE: Configuring authorizeHttpRequests...");
                authorize
                    // Allow access to Swagger UI and API docs without authentication
                    .requestMatchers(SWAGGER_UI_PATHS).permitAll()

                    // VVVVVVVVVV ADDED EXPLICIT RULE FOR PUBLIC ACTUATOR ENDPOINTS VVVVVVVVVV
                    .requestMatchers(PUBLIC_ACTUATOR_PATHS).permitAll()
                    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

                    // Secure your cart service API endpoints
                    .requestMatchers("/api/v1/carts/**").authenticated()

                    // All other requests (including other actuator endpoints not listed in PUBLIC_ACTUATOR_PATHS)
                    // will require authentication.
                    .anyRequest().authenticated();
                }
            )
            .oauth2ResourceServer(oauth2 -> {
                logger.info("CART-SERVICE: Configuring OAuth2 Resource Server with JWT...");
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()));
            })
            .csrf(AbstractHttpConfigurer::disable);

        logger.info("CART-SERVICE: SecurityFilterChain configuration complete.");
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        logger.info("CART-SERVICE: Creating JwtAuthenticationConverter with KeycloakRealmRoleConverter.");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        // Ensure KeycloakRealmRoleConverter class is available in this project's classpath
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }
}