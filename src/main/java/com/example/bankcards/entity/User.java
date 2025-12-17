package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Entity representing a user in the banking system.
 * <p>
 * This entity stores user authentication and authorization information.
 * Users can have multiple bank cards associated with them.
 * <p>
 * User passwords are stored using BCrypt hashing for security.
 * Each user is assigned a role ({@link Role#ADMIN} or {@link Role#USER})
 * that determines their access permissions.
 *
 * @see Card
 * @see Role
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    /**
     * Unique identifier for the user.
     * <p>
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique username for authentication.
     * <p>
     * Must be unique across the system and cannot exceed 100 characters.
     * Used as the principal identifier for Spring Security authentication.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /**
     * Hashed password for authentication.
     * <p>
     * Passwords are stored using BCrypt hashing algorithm.
     * Never store or transmit plain text passwords.
     *
     * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
     */
    @Column(nullable = false)
    private String password;

    /**
     * User's role in the system.
     * <p>
     * Determines access permissions and available operations.
     * Stored as string in the database.
     *
     * @see Role
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * Flag indicating whether the user account is enabled.
     * <p>
     * Disabled users cannot authenticate or access the system.
     * This allows for account suspension without deletion.
     */
    @Column(nullable = false)
    private boolean enabled;

    /**
     * List of bank cards owned by this user.
     * <p>
     * One-to-many relationship where a user can have multiple cards.
     * Lazy-loaded to avoid unnecessary database queries.
     *
     * @see Card
     */
    @OneToMany(mappedBy = "owner")
    private List<Card> cards;
}



