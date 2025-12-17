package com.example.bankcards.controller;

import com.example.bankcards.dto.user.CreateUserRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for administrative user management operations.
 * <p>
 * This controller provides endpoints for administrators to manage user accounts
 * in the system. All endpoints require ADMIN role authentication.
 * <p>
 * Base path: {@code /api/admin/users}
 * <p>
 * Administrative operations:
 * <ul>
 *   <li>Create new user accounts</li>
 *   <li>View all users</li>
 *   <li>View specific user details</li>
 *   <li>Delete user accounts</li>
 * </ul>
 * <p>
 * Security: All endpoints are protected by {@code @PreAuthorize("hasRole('ADMIN')")}
 * and require a valid JWT token with ADMIN role.
 * <p>
 * <strong>Note:</strong> User passwords are never exposed in API responses.
 *
 * @see UserService
 * @see UserResponse
 * @see CreateUserRequest
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;


    /**
     * Creates a new user account.
     * <p>
     * The password is automatically hashed using BCrypt before storage.
     * The username must be unique in the system.
     * New users are created with enabled status by default.
     * <p>
     * <strong>Endpoint:</strong> POST /api/admin/users
     * <p>
     * <strong>Request body example:</strong>
     * <pre>
     * {
     *   "username": "newuser",
     *   "password": "securepassword",
     *   "role": "USER"
     * }
     * </pre>
     *
     * @param request the user creation request containing username, password, and role
     * @return ResponseEntity with UserResponse containing the created user's information (without password)
     * @throws org.springframework.dao.DataIntegrityViolationException if username already exists
     */
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.create(request));
    }

    /**
     * Retrieves all users in the system.
     * <p>
     * Returns a list of all user accounts with their information (excluding passwords).
     * <p>
     * <strong>Endpoint:</strong> GET /api/admin/users
     *
     * @return ResponseEntity with a list of all users
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    /**
     * Retrieves details of a specific user by ID.
     * <p>
     * Returns user information (excluding password).
     * <p>
     * <strong>Endpoint:</strong> GET /api/admin/users/{id}
     *
     * @param id the ID of the user to retrieve
     * @return ResponseEntity with UserResponse containing user information
     * @throws com.example.bankcards.exception.NotFoundException if no user exists with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    /**
     * Permanently deletes a user account from the system.
     * <p>
     * This operation cannot be undone. All user data will be permanently removed.
     * <p>
     * <strong>Warning:</strong> Deleting a user may affect associated cards.
     * Ensure proper cleanup of related entities before deletion.
     * <p>
     * <strong>Endpoint:</strong> DELETE /api/admin/users/{id}
     *
     * @param id the ID of the user to delete
     * @return ResponseEntity with 204 No Content status on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}



