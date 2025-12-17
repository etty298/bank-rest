package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) for creating a new bank card.
 * <p>
 * This record encapsulates the data required by administrators to create
 * a new card for a user. All fields are validated for presence.
 * <p>
 * The card number will be encrypted before storage using AES-256 encryption.
 * The expiration date should be set to a future date.
 * <p>
 * Example request:
 * <pre>
 * {
 *   "userId": 1,
 *   "cardNumber": "1234567890123456",
 *   "expirationDate": "2025-12-31"
 * }
 * </pre>
 * <p>
 * <strong>Note:</strong> This operation is restricted to administrators only.
 *
 * @param userId the ID of the user who will own this card (must not be null)
 * @param cardNumber the card number in plain text (must not be blank, will be encrypted)
 * @param expirationDate the card's expiration date (must not be null, should be in the future)
 * @see com.example.bankcards.controller.AdminCardController
 * @see com.example.bankcards.util.CryptoUtil
 */
public record CreateCardRequest(
        @NotNull Long userId,
        @NotBlank String cardNumber,
        @NotNull LocalDate expirationDate
) {}



