package com.appvese.notification_service.config; // Correct package

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Ensure Customizer is imported if you decide to use .jwt(Customizer.withDefaults()) later
// import org.springframework.security.config.Customizer;
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
        http
            .authorizeHttpRequests(authorize -> authorize
                // Allow access to Swagger UI and API docs without authentication
                .requestMatchers(SWAGGER_UI_PATHS).permitAll()

                // Secure your ORDER service API endpoints
                .requestMatchers("/api/v1/orders/**").authenticated() // <<< CORRECTED PATH for order-service
                // Add other specific paths for order-service if they have different security needs

                // All other requests (if any not covered above) should be authenticated
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        // Ensure KeycloakRealmRoleConverter class is available in this service's classpath
        // (e.g., in a shared common module, or copied to this project)
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }
}