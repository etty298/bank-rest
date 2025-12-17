package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for {@link Card} entity database operations.
 * <p>
 * This interface extends {@link JpaRepository} to provide CRUD operations
 * and custom query methods for managing bank cards. Spring Data JPA
 * automatically implements this interface at runtime.
 * <p>
 * Custom query methods include:
 * <ul>
 *   <li>Finding cards by owner with pagination support</li>
 *   <li>Finding cards by ID and owner (for authorization checks)</li>
 *   <li>Checking existence of encrypted card numbers (for uniqueness validation)</li>
 * </ul>
 *
 * @see Card
 * @see User
 * @see JpaRepository
 */
public interface CardRepository extends JpaRepository<Card, Long> {
    
    /**
     * Finds all cards belonging to a specific owner with pagination support.
     * <p>
     * This method is used to retrieve a user's cards in paginated format,
     * allowing for efficient display of large card collections.
     *
     * @param owner the user who owns the cards
     * @param pageable the pagination information (page number, size, sort)
     * @return a page of cards belonging to the specified owner
     */
    Page<Card> findAllByOwner(User owner, Pageable pageable);
    
    /**
     * Finds a card by its ID and owner ID.
     * <p>
     * This method is used for authorization checks to ensure that a user
     * can only access their own cards. It returns empty if the card doesn't
     * exist or doesn't belong to the specified owner.
     *
     * @param id the card ID to search for
     * @param ownerId the owner's user ID
     * @return an Optional containing the card if found and owned by the user, empty otherwise
     */
    Optional<Card> findByIdAndOwnerId(Long id, Long ownerId);
    
    /**
     * Checks whether a card with the given encrypted card number already exists.
     * <p>
     * This method is used to ensure uniqueness of card numbers in the system
     * during card creation. Each card number (in encrypted form) must be unique.
     *
     * @param encryptedCardNumber the encrypted card number to check
     * @return true if a card with this encrypted number exists, false otherwise
     */
    boolean existsByEncryptedCardNumber(String encryptedCardNumber);
}



