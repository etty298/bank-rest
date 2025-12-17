package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing a bank card in the system.
 * <p>
 * This entity stores card information including encrypted card numbers,
 * balance, status, and ownership details. Card numbers are encrypted using
 * AES-256 encryption before storage for security.
 * <p>
 * Cards have a status that determines their usability:
 * <ul>
 *   <li>{@link CardStatus#ACTIVE} - Can be used for all operations</li>
 *   <li>{@link CardStatus#BLOCKED} - Temporarily suspended</li>
 *   <li>{@link CardStatus#EXPIRED} - Past expiration date</li>
 * </ul>
 * <p>
 * Only ACTIVE cards can participate in money transfers.
 *
 * @see User
 * @see CardStatus
 * @see com.example.bankcards.util.CryptoUtil
 * @see com.example.bankcards.util.MaskingUtil
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cards")
public class Card {
    /**
     * Unique identifier for the card.
     * <p>
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Encrypted card number stored in the database.
     * <p>
     * Card numbers are encrypted using AES-256 encryption for security.
     * The plain text card number is never stored in the database.
     * When displayed to users, the card number is masked (e.g., "**** **** **** 1234").
     * <p>
     * Maximum length of 512 characters to accommodate encrypted data.
     *
     * @see com.example.bankcards.util.CryptoUtil#encrypt(String)
     * @see com.example.bankcards.util.MaskingUtil#maskCardNumber(String)
     */
    @Column(name = "encrypted_card_number", nullable = false, unique = true, length = 512)
    private String encryptedCardNumber;

    /**
     * The user who owns this card.
     * <p>
     * Many-to-one relationship where multiple cards can belong to one user.
     * Lazy-loaded to optimize performance.
     *
     * @see User
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Expiration date of the card.
     * <p>
     * Cards cannot be used after this date and their status should be set to EXPIRED.
     * Typically set to a future date when the card is created.
     */
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    /**
     * Current status of the card.
     * <p>
     * Determines whether the card can be used for transactions.
     * Stored as string in the database.
     *
     * @see CardStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status;

    /**
     * Current balance on the card.
     * <p>
     * Stored with precision of 19 digits and scale of 2 decimal places
     * to accurately represent monetary values. Balance is updated during
     * transfer operations and must never be negative.
     * <p>
     * Example: 1234567890.12
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
}



