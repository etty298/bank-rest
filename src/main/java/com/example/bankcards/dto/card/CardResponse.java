package com.example.bankcards.dto.card;

import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) for card information response.
 * <p>
 * This record encapsulates card data sent to clients. For security reasons,
 * the card number is always masked, showing only the last 4 digits.
 * <p>
 * The masked card number follows the format: {@code **** **** **** 1234}
 * <p>
 * Example response:
 * <pre>
 * {
 *   "id": 1,
 *   "maskedCardNumber": "**** **** **** 1234",
 *   "expirationDate": "2025-12-31",
 *   "status": "ACTIVE",
 *   "balance": 1000.50
 * }
 * </pre>
 *
 * @param id the unique identifier of the card
 * @param maskedCardNumber the masked card number (e.g., "**** **** **** 1234")
 * @param expirationDate the card's expiration date
 * @param status the current status of the card (ACTIVE, BLOCKED, or EXPIRED)
 * @param balance the current balance on the card
 * @see com.example.bankcards.entity.Card
 * @see com.example.bankcards.util.MaskingUtil
 * @see CardStatus
 */
public record CardResponse(
        Long id,
        String maskedCardNumber,
        LocalDate expirationDate,
        CardStatus status,
        BigDecimal balance
) {}



