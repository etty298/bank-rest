package com.example.bankcards.dto.card;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) for transferring money between cards.
 * <p>
 * This record encapsulates the data required to transfer funds from one
 * card to another. Both cards must belong to the authenticated user.
 * <p>
 * Validation rules:
 * <ul>
 *   <li>All fields must not be null</li>
 *   <li>Amount must be at least 1</li>
 *   <li>Both cards must be ACTIVE status</li>
 *   <li>Source card must have sufficient balance</li>
 *   <li>Both cards must belong to the same user</li>
 * </ul>
 * <p>
 * Example request:
 * <pre>
 * {
 *   "fromCardId": 1,
 *   "toCardId": 2,
 *   "amount": 100.00
 * }
 * </pre>
 *
 * @param fromCardId the ID of the source card (must not be null, must be owned by user)
 * @param toCardId the ID of the destination card (must not be null, must be owned by user)
 * @param amount the amount to transfer (must not be null, must be at least 1)
 * @see com.example.bankcards.controller.CardController
 * @see com.example.bankcards.service.CardService
 */
public record TransferRequest(
        @NotNull Long fromCardId,
        @NotNull Long toCardId,
        @NotNull @Min(1) BigDecimal amount
) {}



