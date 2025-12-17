package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for {@link User} entity database operations.
 * <p>
 * This interface extends {@link JpaRepository} to provide CRUD operations
 * and custom query methods for managing users. Spring Data JPA
 * automatically implements this interface at runtime.
 * <p>
 * Custom query methods include:
 * <ul>
 *   <li>Finding users by username (for authentication)</li>
 *   <li>Checking username existence (for uniqueness validation)</li>
 * </ul>
 *
 * @see User
 * @see JpaRepository
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds a user by their unique username.
     * <p>
     * This method is primarily used during authentication to load user
     * details for login. It returns empty if no user with the given
     * username exists.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Checks whether a user with the given username already exists.
     * <p>
     * This method is used to ensure uniqueness of usernames during
     * user registration and updates. Each username must be unique in the system.
     *
     * @param username the username to check
     * @return true if a user with this username exists, false otherwise
     */
    boolean existsByUsername(String username);
}



