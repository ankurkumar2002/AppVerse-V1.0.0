package com.appverse.api_gateway.config;

// No need for java.security.Security import unless used elsewhere

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // For disabling CSRF
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Often good practice to add
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Explicitly enable web security configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Use authorizeHttpRequests instead of authorizeRequests
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/developers/**").authenticated()// Logic: allow all requests to developer service
                .requestMatchers("/api/apps/**").authenticated() // Logic: allow all requests to application service
                .requestMatchers("/api/v1/users/**").authenticated()
                .requestMatchers("/api/v1/carts/**").authenticated() // Logic: require authentication for cart service
                .requestMatchers("/api/v1/orders/**").authenticated() // Logic: require authentication for order service
                .requestMatchers("/api/v1/payments/**").authenticated() // Logic: require authentication for payment service
                .requestMatchers("/api/v1/subscriptions/**").authenticated() // Logic: require authentication for subscription service
                .anyRequest().permitAll() // Logic: require authentication for any request
            )
            // Configure OAuth2 Resource Server with JWT support using defaults
            // This part remains the same and reads config from application.properties
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            );

        // Optional but Recommended for stateless APIs (like gateways/resource servers): Disable CSRF
        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}