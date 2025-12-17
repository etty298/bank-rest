package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CreateCardRequest;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.entity.*;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CryptoUtil;
import com.example.bankcards.util.MaskingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service for managing bank card operations.
 * <p>
 * This service provides both user-facing and administrative operations for cards:
 * <ul>
 *   <li><strong>User operations:</strong> View own cards, check balances, transfer funds</li>
 *   <li><strong>Admin operations:</strong> Create cards, activate/block cards, delete cards, view all cards</li>
 * </ul>
 * <p>
 * Security features:
 * <ul>
 *   <li>Card numbers are encrypted using AES-256 before storage</li>
 *   <li>Card numbers are masked when displayed (only last 4 digits visible)</li>
 *   <li>Users can only access their own cards</li>
 *   <li>Transfer validation ensures both cards belong to the user and are ACTIVE</li>
 * </ul>
 * <p>
 * All methods are transactional to ensure data consistency during operations.
 *
 * @see com.example.bankcards.controller.CardController
 * @see com.example.bankcards.controller.AdminCardController
 * @see Card
 * @see CardResponse
 */
@Service
@RequiredArgsConstructor
@Transactional()
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CryptoUtil cryptoUtil;


    /**
     * Retrieves a specific card owned by the current user.
     * <p>
     * This method ensures the user can only access their own cards.
     * The card number is masked in the response for security.
     *
     * @param id the ID of the card to retrieve
     * @return CardResponse with masked card number and card details
     * @throws NotFoundException if the card doesn't exist or doesn't belong to the user
     * @see CardResponse
     */
    public CardResponse getOwn(Long id) {
        User current = getCurrentUser();
        Card card = cardRepository.findByIdAndOwnerId(id, current.getId())
                .orElseThrow(() -> new NotFoundException("Card not found: " + id));
        return toResponse(card);
    }

    /**
     * Retrieves all cards owned by the current user with pagination.
     * <p>
     * Returns a paginated list of the user's cards. Card numbers are masked.
     *
     * @param pageable pagination information (page number, size, sort)
     * @return a page of CardResponse objects for the current user
     * @see CardResponse
     * @see Pageable
     */
    public Page<CardResponse> getOwn(Pageable pageable) {
        User current = getCurrentUser();
        return cardRepository.findAllByOwner(current, pageable).map(this::toResponse);
    }

    /**
     * Retrieves the balance of a card owned by the current user.
     * <p>
     * This method verifies ownership before returning the balance.
     *
     * @param id the ID of the card
     * @return the current balance of the card
     * @throws NotFoundException if the card doesn't exist
     * @throws AccessDeniedException if the card doesn't belong to the current user
     */
    public BigDecimal getBalance(Long id) {
        return getOwnEntity(id).getBalance();
    }

    /**
     * Transfers money between two cards owned by the current user.
     * <p>
     * This method performs extensive validation:
     * <ul>
     *   <li>Source and destination cards must be different</li>
     *   <li>Amount must be positive</li>
     *   <li>Both cards must belong to the current user</li>
     *   <li>Both cards must have ACTIVE status</li>
     *   <li>Source card must have sufficient balance</li>
     * </ul>
     * <p>
     * The transfer is atomic - either both balance updates succeed or none do.
     *
     * @param request the transfer request containing source card, destination card, and amount
     * @throws IllegalArgumentException if validation fails (same cards, invalid amount, inactive cards, insufficient funds)
     * @throws NotFoundException if either card doesn't exist
     * @throws AccessDeniedException if either card doesn't belong to the current user
     * @see TransferRequest
     */
    @Transactional
    public void transfer(TransferRequest request) {
        if (request.fromCardId().equals(request.toCardId())) {
            throw new IllegalArgumentException("fromCardId must not equal toCardId");
        }
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Card from = getOwnEntity(request.fromCardId());
        Card to = getOwnEntity(request.toCardId());
        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Both cards must be ACTIVE");
        }
        if (from.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        from.setBalance(from.getBalance().subtract(request.amount()));
        to.setBalance(to.getBalance().add(request.amount()));
        cardRepository.save(from);
        cardRepository.save(to);
    }

    /**
     * Creates a new card for a specific user (admin operation).
     * <p>
     * This method:
     * <ol>
     *   <li>Verifies the user exists</li>
     *   <li>Encrypts the card number using AES-256</li>
     *   <li>Creates the card with BLOCKED status and zero balance</li>
     *   <li>Saves the card to the database</li>
     * </ol>
     * <p>
     * <strong>Note:</strong> New cards are created with BLOCKED status for security.
     * Administrators must explicitly activate them.
     *
     * @param request the card creation request containing user ID, card number, and expiration date
     * @return CardResponse with masked card number
     * @throws NotFoundException if the specified user doesn't exist
     * @see CreateCardRequest
     * @see CardResponse
     */
    public CardResponse createForUser(CreateCardRequest request) {
        User user = userRepository.findById(request.userId()).orElseThrow(() -> new NotFoundException("User not found: " + request.userId()));
        String encrypted = cryptoUtil.encrypt(request.cardNumber());
        Card card = Card.builder()
                .encryptedCardNumber(encrypted)
                .owner(user)
                .expirationDate(request.expirationDate())
                .status(CardStatus.BLOCKED)
                .balance(BigDecimal.ZERO)
                .build();
        card = cardRepository.save(card);
        return toResponse(card);
    }

    /**
     * Activates a blocked card (admin operation).
     * <p>
     * Changes the card status to ACTIVE, allowing it to be used for transactions.
     *
     * @param id the ID of the card to activate
     * @return CardResponse with updated status
     * @throws NotFoundException if the card doesn't exist
     * @see CardStatus
     */
    public CardResponse activate(Long id) {
        Card card = getById(id);
        card.setStatus(CardStatus.ACTIVE);
        return toResponse(cardRepository.save(card));
    }

    /**
     * Blocks an active card (admin operation).
     * <p>
     * Changes the card status to BLOCKED, preventing it from being used for transactions.
     * This is typically used for security purposes or account suspension.
     *
     * @param id the ID of the card to block
     * @return CardResponse with updated status
     * @throws NotFoundException if the card doesn't exist
     * @see CardStatus
     */
    public CardResponse block(Long id) {
        Card card = getById(id);
        card.setStatus(CardStatus.BLOCKED);
        return toResponse(cardRepository.save(card));
    }

    /**
     * Deletes a card from the system (admin operation).
     * <p>
     * This permanently removes the card and all associated data.
     * <strong>Warning:</strong> This operation cannot be undone.
     *
     * @param id the ID of the card to delete
     */
    public void delete(Long id) { cardRepository.deleteById(id); }

    /**
     * Retrieves all cards in the system with pagination (admin operation).
     * <p>
     * Returns a paginated list of all cards regardless of owner.
     * Card numbers are masked in responses.
     *
     * @param pageable pagination information (page number, size, sort)
     * @return a page of all cards in the system
     * @see Pageable
     */
    public Page<CardResponse> getAll(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::toResponse);
    }

    /**
     * Retrieves a card entity by ID (internal helper method).
     * <p>
     * Used by admin operations that don't require ownership verification.
     *
     * @param id the ID of the card to retrieve
     * @return the Card entity
     * @throws NotFoundException if the card doesn't exist
     */
    private Card getById(Long id) {
        return cardRepository.findById(id).orElseThrow(() -> new NotFoundException("Card not found: " + id));
    }

    /**
     * Retrieves a card entity owned by the current user (internal helper method).
     * <p>
     * This method performs ownership verification and is used by user-facing operations.
     *
     * @param id the ID of the card to retrieve
     * @return the Card entity if owned by current user
     * @throws NotFoundException if the card doesn't exist
     * @throws AccessDeniedException if the card doesn't belong to the current user
     */
    private Card getOwnEntity(Long id) {
        User current = getCurrentUser();
        Card card = cardRepository.findById(id).orElseThrow(() -> new NotFoundException("Card not found: " + id));
        if (!card.getOwner().getId().equals(current.getId())) {
            throw new AccessDeniedException("You don't own this card");
        }
        return card;
    }

    /**
     * Converts a Card entity to a CardResponse DTO (internal helper method).
     * <p>
     * This method:
     * <ol>
     *   <li>Decrypts the card number</li>
     *   <li>Masks the card number (shows only last 4 digits)</li>
     *   <li>Creates a CardResponse with masked number and other details</li>
     * </ol>
     *
     * @param card the Card entity to convert
     * @return CardResponse with masked card number
     */
    private CardResponse toResponse(Card card) {
        String number = cryptoUtil.decrypt(card.getEncryptedCardNumber());
        return new CardResponse(card.getId(), MaskingUtil.maskCardNumber(number), card.getExpirationDate(), card.getStatus(), card.getBalance());
    }

    /**
     * Retrieves the currently authenticated user (internal helper method).
     * <p>
     * Extracts the username from the security context and loads the user from database.
     *
     * @return the currently authenticated User
     * @throws RuntimeException if no user is found (should not happen with proper authentication)
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username).orElseThrow();
    }
}



