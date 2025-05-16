package com.appverse.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

     private static final String[] SWAGGER_UI_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/webjars/**"
    };

    private static final String[] SWAGGER_AGGREGATE_PATHS = {
            "/aggregate/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(SWAGGER_UI_PATHS).permitAll()
                .requestMatchers(SWAGGER_AGGREGATE_PATHS).permitAll()
                .requestMatchers("/fallback").permitAll() // <<< ADD THIS TO PERMIT FALLBACK
                .requestMatchers("/api/developers/**").authenticated()
                .requestMatchers("/api/apps/**").authenticated()
                // Ensure /api/categories/** is also handled if it's a distinct secured path
                .requestMatchers("/api/categories/**").authenticated() // <<< ADD IF NEEDED
                .requestMatchers("/api/v1/users/**").authenticated()
                .requestMatchers("/api/v1/carts/**").authenticated()
                .requestMatchers("/api/v1/orders/**").authenticated()
                .requestMatchers("/api/v1/payments/**").authenticated()
                .requestMatchers("/api/v1/subscriptions/**").authenticated()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            )
            .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}