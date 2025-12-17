package com.example.bankcards.dto.user;

import com.example.bankcards.entity.Role;

/**
 * Data Transfer Object (DTO) for user information response.
 * <p>
 * This record encapsulates user data sent to clients. The password
 * is never included in responses for security reasons.
 * <p>
 * Example response:
 * <pre>
 * {
 *   "id": 1,
 *   "username": "admin",
 *   "role": "ADMIN",
 *   "enabled": true
 * }
 * </pre>
 *
 * @param id the unique identifier of the user
 * @param username the user's username
 * @param role the user's role (ADMIN or USER)
 * @param enabled whether the user account is enabled
 * @see com.example.bankcards.entity.User
 * @see Role
 */
public record UserResponse(Long id, String username, Role role, boolean enabled) {}



