package com.example.bankcards.dto.auth;

/**
 * Data Transfer Object (DTO) for JWT authentication response.
 * <p>
 * This record encapsulates the authentication response sent to clients
 * after successful login. It contains the JWT token that must be included
 * in subsequent requests for authentication.
 * <p>
 * The token should be sent in the Authorization header as: {@code Bearer <token>}
 * <p>
 * Example response:
 * <pre>
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "username": "admin",
 *   "role": "ADMIN"
 * }
 * </pre>
 *
 * @param token the JWT token for authentication
 * @param username the authenticated user's username
 * @param role the user's role (ADMIN or USER)
 * @see com.example.bankcards.controller.AuthController
 * @see com.example.bankcards.security.JwtTokenProvider
 */
public record JwtResponse(String token, String username, String role) {}



