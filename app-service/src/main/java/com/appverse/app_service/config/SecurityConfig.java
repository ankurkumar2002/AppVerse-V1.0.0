package com.appverse.app_service.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
            "/webjars/**",
            "/actuator/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring SecurityFilterChain in app-service...");

        http
                .addFilterBefore((request, response, chain) -> {
                    String authHeader = ((jakarta.servlet.http.HttpServletRequest) request).getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        logger.debug("APP-SERVICE: Authorization Bearer token present.");
                    } else {
                        String requestURI = ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI();
                        boolean isSwaggerPath = false;
                        for (String path : SWAGGER_UI_PATHS) {
                            if (requestURI.equals(path.replace("/**", ""))
                                    || requestURI.startsWith(path.replace("/**", ""))) {
                                if (path.endsWith("/**") && requestURI.startsWith(path.substring(0, path.length() - 3)))
                                    isSwaggerPath = true;
                                else if (requestURI.equals(path))
                                    isSwaggerPath = true;
                                if (isSwaggerPath)
                                    break;
                            }
                        }
                        if (!isSwaggerPath) {
                            logger.warn("APP-SERVICE: No/Invalid Authorization Bearer token found in request to {}",
                                    requestURI);
                        }
                    }
                    chain.doFilter(request, response);
                }, AuthorizationFilter.class)
                .authorizeHttpRequests(authorize -> {
                    logger.info("APP-SERVICE: Configuring authorizeHttpRequests...");
                    authorize
                            .requestMatchers(SWAGGER_UI_PATHS).permitAll()
                            .requestMatchers("/api/apps/test-public").permitAll()
                            .requestMatchers("/api/apps/**").authenticated()
                            .requestMatchers("/actuator/**").permitAll()
                            .anyRequest().authenticated();
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

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            List<GrantedAuthority> authorities = new ArrayList<>();

            // your existing Keycloak roles
            authorities.addAll(
                    new KeycloakRealmRoleConverter().convert(jwt));

            // OAuth scopes
            JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

            scopeConverter.setAuthorityPrefix("SCOPE_");
            scopeConverter.setAuthoritiesClaimName("scope");

            authorities.addAll(scopeConverter.convert(jwt));

            return authorities;
        });

        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:4200"));

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
