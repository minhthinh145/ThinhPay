package com.thinhpay.backend.shared.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Global Security Configuration - Shared Infrastructure.
 *
 * Design: Cross-cutting concern, không thuộc bất kỳ domain module nào.
 * Tất cả modules (IAM, CoreBanking, Payment) đều dùng chung → tuân thủ DDD.
 *
 * Features:
 * - JWT-based authentication
 * - Role-based authorization
 * - CORS configuration
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;

    /**
     * Security filter chain - JWT authentication + authorization.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (không cần với JWT)
            .csrf(AbstractHttpConfigurer::disable)

            // CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless session (JWT không cần session)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (không cần authentication)
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/api/v1/health",
                    "/swagger-ui.html",           // Swagger UI landing page
                    "/swagger-ui/**",             // Swagger UI resources
                    "/v3/api-docs",               // OpenAPI docs (exact match)
                    "/v3/api-docs/**",            // OpenAPI subdocuments
                    "/swagger-resources/**",      // Swagger resources
                    "/webjars/**",                // Swagger UI webjars
                    "/actuator/health",           // Health check (public)
                    "/actuator/health/**",        // Health check details
                    "/actuator/info",             // Application info (public)
                    "/actuator/prometheus"        // Prometheus metrics (public for scraping)
                ).permitAll()

                // Admin endpoints
                .requestMatchers("/api/v1/admin/**", "/actuator/**").hasRole("ADMIN")

                // Merchant endpoints
                .requestMatchers("/api/v1/merchant/**").hasAnyRole("ADMIN", "MERCHANT")

                // All other requests need authentication
                .anyRequest().authenticated()
            )

            // Add Rate Limiting filter FIRST (before everything)
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)

            // Add JWT filter AFTER rate limiting but BEFORE username/password
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * PasswordEncoder bean sử dụng BCrypt algorithm.
     * BCrypt tự động thêm salt và có cost factor để chống brute-force.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Cost factor 12 for security
    }

    /**
     * CORS configuration - Allow frontend access.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",  // Angular dev
            "http://localhost:3000"   // React dev (if needed)
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}


