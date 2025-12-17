package com.example.bankcards.config;

import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.RestAuthEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

/**
 * Security configuration for the Bank Cards REST API.
 * <p>
 * This configuration class sets up Spring Security with the following features:
 * <ul>
 *   <li>JWT-based authentication using custom filter</li>
 *   <li>Stateless session management (no server-side sessions)</li>
 *   <li>Method-level security with {@code @PreAuthorize} annotations</li>
 *   <li>BCrypt password encoding</li>
 *   <li>CORS configuration with default permissive settings</li>
 *   <li>CSRF protection disabled (suitable for stateless REST APIs)</li>
 * </ul>
 * <p>
 * Public endpoints that don't require authentication:
 * <ul>
 *   <li>/api/auth/** - Authentication endpoints (login)</li>
 *   <li>/swagger-ui/** - API documentation</li>
 *   <li>/v3/api-docs/** - OpenAPI specification</li>
 *   <li>/actuator/** - Spring Boot actuator endpoints</li>
 * </ul>
 * <p>
 * All other endpoints require JWT authentication.
 *
 * @see JwtAuthenticationFilter
 * @see RestAuthEntryPoint
 * @see EnableMethodSecurity
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthEntryPoint restAuthEntryPoint;


    /**
     * Configures the security filter chain for HTTP requests.
     * <p>
     * This method sets up:
     * <ul>
     *   <li>CSRF protection disabled (not needed for stateless REST APIs)</li>
     *   <li>CORS with default permissive configuration</li>
     *   <li>Stateless session management (no HttpSession created)</li>
     *   <li>Custom authentication entry point for unauthorized requests</li>
     *   <li>Authorization rules for endpoints</li>
     *   <li>JWT authentication filter before username/password filter</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(restAuthEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/docs/**",
                                "/api/auth/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Provides a password encoder bean for hashing passwords.
     * <p>
     * Uses BCrypt hashing algorithm with a default strength of 10.
     * BCrypt is a strong adaptive hash function designed for password hashing.
     *
     * @return a {@link BCryptPasswordEncoder} instance
     * @see BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides an authentication manager bean for processing authentication requests.
     * <p>
     * The authentication manager is used by the authentication controller
     * to authenticate user credentials during login.
     *
     * @param configuration the authentication configuration
     * @return the {@link AuthenticationManager} instance
     * @throws Exception if an error occurs during configuration
     * @see com.example.bankcards.controller.AuthController
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}



