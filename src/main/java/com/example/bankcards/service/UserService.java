package com.example.bankcards.service;

import com.example.bankcards.dto.user.CreateUserRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing user accounts in the banking system.
 * <p>
 * This service provides administrative operations for user management including:
 * <ul>
 *   <li>Creating new user accounts with encrypted passwords</li>
 *   <li>Retrieving user information</li>
 *   <li>Deleting user accounts</li>
 * </ul>
 * <p>
 * All operations in this service are transactional to ensure data consistency.
 * Passwords are automatically hashed using BCrypt before storage.
 * <p>
 * <strong>Note:</strong> All methods in this service are restricted to
 * administrators only and should be called from admin-only controllers.
 *
 * @see com.example.bankcards.controller.AdminUserController
 * @see User
 * @see UserResponse
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * Creates a new user account with the specified details.
     * <p>
     * This method:
     * <ol>
     *   <li>Hashes the password using BCrypt</li>
     *   <li>Creates a new user with enabled status</li>
     *   <li>Saves the user to the database</li>
     *   <li>Returns the created user information (without password)</li>
     * </ol>
     * <p>
     * The username must be unique in the system. If a duplicate username
     * is provided, a database constraint violation will occur.
     *
     * @param request the user creation request containing username, password, and role
     * @return UserResponse containing the created user's information
     * @throws org.springframework.dao.DataIntegrityViolationException if username already exists
     * @see CreateUserRequest
     * @see UserResponse
     */
    public UserResponse create(CreateUserRequest request) {
        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .enabled(true)
                .build();
        user = userRepository.save(user);
        return map(user);
    }

    /**
     * Retrieves all users in the system.
     * <p>
     * Returns a list of all users with their information (excluding passwords).
     * This method is intended for administrative purposes.
     *
     * @return a list of all users in the system
     * @see UserResponse
     */
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::map).toList();
    }

    /**
     * Retrieves a specific user by their ID.
     * <p>
     * Returns the user's information (excluding password) if found.
     *
     * @param id the ID of the user to retrieve
     * @return UserResponse containing the user's information
     * @throws NotFoundException if no user exists with the given ID
     * @see UserResponse
     */
    public UserResponse findById(Long id) {
        return map(getById(id));
    }

    /**
     * Deletes a user account from the system.
     * <p>
     * This operation permanently removes the user and all associated data.
     * <strong>Warning:</strong> This operation cannot be undone.
     * <p>
     * <strong>Note:</strong> Deleting a user may affect associated cards.
     * Ensure proper cascade handling or manual cleanup of related entities.
     *
     * @param id the ID of the user to delete
     */
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Retrieves a user entity by ID.
     * <p>
     * This is a helper method used internally by the service.
     * Returns the full User entity (including password hash).
     *
     * @param id the ID of the user to retrieve
     * @return the User entity
     * @throws NotFoundException if no user exists with the given ID
     */
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    /**
     * Converts a User entity to a UserResponse DTO.
     * <p>
     * This helper method maps the internal User entity to a DTO suitable
     * for client responses. The password is excluded for security.
     *
     * @param user the User entity to convert
     * @return UserResponse DTO containing user information
     */
    private UserResponse map(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.isEnabled());
    }
}



