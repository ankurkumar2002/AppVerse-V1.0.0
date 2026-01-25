
package com.appverse.cart_service.config; 

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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class); 

    private static final String[] SWAGGER_UI_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/webjars/**"
    };

    private static final String[] PUBLIC_ACTUATOR_PATHS = {
            "/actuator/prometheus",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring SecurityFilterChain in cart-service...");
        http
                .authorizeHttpRequests(authorize -> {
                    logger.info("CART-SERVICE: Configuring authorizeHttpRequests...");
                    authorize
                            .requestMatchers(SWAGGER_UI_PATHS).permitAll()
                            .requestMatchers(PUBLIC_ACTUATOR_PATHS).permitAll()
                            .requestMatchers("/api/v1/carts/**").authenticated()
                            .anyRequest().authenticated();
                })
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
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }
}