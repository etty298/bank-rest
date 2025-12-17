package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) for user login request.
 * <p>
 * This record encapsulates the credentials required for user authentication.
 * Both fields are validated to ensure they are not blank.
 * <p>
 * Example request:
 * <pre>
 * {
 *   "username": "admin",
 *   "password": "admin"
 * }
 * </pre>
 * <p>
 * Upon successful authentication, the server responds with a {@link JwtResponse}
 * containing the JWT token.
 *
 * @param username the user's username (must not be blank)
 * @param password the user's password (must not be blank)
 * @see JwtResponse
 * @see com.example.bankcards.controller.AuthController
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {}



