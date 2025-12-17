package com.example.bankcards.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security filter for JWT-based authentication.
 * <p>
 * This filter intercepts incoming HTTP requests and extracts JWT tokens
 * from the Authorization header. If a valid token is found, it authenticates
 * the user and sets the authentication in the Spring Security context.
 * <p>
 * The filter expects tokens in the format: {@code Bearer <token>}
 * <p>
 * This filter extends {@link OncePerRequestFilter} to ensure it's executed
 * exactly once per request, even with request forwarding or error handling.
 * <p>
 * Authentication flow:
 * <ol>
 *   <li>Extract Authorization header from request</li>
 *   <li>Check if header starts with "Bearer "</li>
 *   <li>Extract and validate JWT token</li>
 *   <li>Load user details from database</li>
 *   <li>Create authentication object and set it in SecurityContext</li>
 *   <li>Continue with filter chain</li>
 * </ol>
 *
 * @see JwtTokenProvider
 * @see CustomUserDetailsService
 * @see com.example.bankcards.config.SecurityConfig
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;


    /**
     * Performs JWT authentication for each request.
     * <p>
     * This method is called for every HTTP request that passes through
     * the security filter chain. It extracts the JWT token from the
     * Authorization header, validates it, and authenticates the user.
     * <p>
     * If no token is present or the token is invalid, the request continues
     * without authentication (allowing public endpoints to work).
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtTokenProvider.validate(token)) {
                String username = jwtTokenProvider.getUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}



