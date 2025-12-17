package com.example.bankcards.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Custom authentication entry point for REST API authentication failures.
 * <p>
 * This component handles authentication exceptions by returning a JSON response
 * instead of redirecting to a login page (which is the default behavior).
 * This is appropriate for REST APIs where clients expect JSON responses.
 * <p>
 * When authentication fails (e.g., missing or invalid JWT token), this entry point:
 * <ul>
 *   <li>Sets HTTP status to 401 Unauthorized</li>
 *   <li>Sets content type to application/json</li>
 *   <li>Returns a JSON error response with error details</li>
 * </ul>
 * <p>
 * Example error response:
 * <pre>
 * {
 *   "error": "Unauthorized",
 *   "message": "Full authentication is required to access this resource"
 * }
 * </pre>
 *
 * @see AuthenticationEntryPoint
 * @see com.example.bankcards.config.SecurityConfig
 */
@Component
public class RestAuthEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handles authentication failures by returning a JSON error response.
     * <p>
     * This method is invoked by Spring Security when a user tries to access
     * a protected resource without proper authentication.
     *
     * @param request the HTTP request that resulted in authentication failure
     * @param response the HTTP response to send to the client
     * @param authException the exception that caused authentication failure
     * @throws IOException if an error occurs while writing the response
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "error", "Unauthorized",
                "message", authException.getMessage()
        ));
    }
}



