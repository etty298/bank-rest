package com.example.bankcards.dto.user;

import com.example.bankcards.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object (DTO) for creating a new user.
 * <p>
 * This record encapsulates the data required by administrators to create
 * a new user account in the system. All fields are validated for presence.
 * <p>
 * The password will be hashed using BCrypt before storage. The username
 * must be unique in the system.
 * <p>
 * Example request:
 * <pre>
 * {
 *   "username": "newuser",
 *   "password": "securepassword",
 *   "role": "USER"
 * }
 * </pre>
 * <p>
 * <strong>Note:</strong> This operation is restricted to administrators only.
 * New users are created with {@code enabled=true} by default.
 *
 * @param username the unique username (must not be blank, must be unique)
 * @param password the password in plain text (must not be blank, will be hashed)
 * @param role the user's role (must not be null, either ADMIN or USER)
 * @see com.example.bankcards.controller.AdminUserController
 * @see com.example.bankcards.entity.Role
 */
public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotNull Role role
) {}



