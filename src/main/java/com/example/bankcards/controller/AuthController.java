package com.example.bankcards.controller;

import com.example.bankcards.dto.auth.JwtResponse;
import com.example.bankcards.dto.auth.LoginRequest;
import com.example.bankcards.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * <p>
 * This controller handles user authentication and JWT token generation.
 * It provides public endpoints that don't require authentication.
 * <p>
 * Base path: {@code /api/auth}
 * <p>
 * Available endpoints:
 * <ul>
 *   <li>POST /api/auth/login - Authenticate user and get JWT token</li>
 * </ul>
 *
 * @see AuthService
 * @see JwtResponse
 * @see LoginRequest
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;


    /**
     * Authenticates a user and returns a JWT token.
     * <p>
     * This endpoint validates user credentials and generates a JWT token
     * for successful authentication. The token should be included in the
     * Authorization header for subsequent requests.
     * <p>
     * <strong>Endpoint:</strong> POST /api/auth/login
     * <p>
     * <strong>Request body example:</strong>
     * <pre>
     * {
     *   "username": "admin",
     *   "password": "admin"
     * }
     * </pre>
     * <p>
     * <strong>Response example:</strong>
     * <pre>
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "username": "admin",
     *   "role": "ADMIN"
     * }
     * </pre>
     *
     * @param request the login request containing username and password
     * @return ResponseEntity with JwtResponse containing token and user details
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}



