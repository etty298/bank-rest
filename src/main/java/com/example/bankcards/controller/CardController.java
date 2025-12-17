package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST controller for user card operations.
 * <p>
 * This controller provides endpoints for authenticated users to manage
 * their own bank cards. All endpoints require JWT authentication.
 * <p>
 * Base path: {@code /api/cards}
 * <p>
 * Available operations:
 * <ul>
 *   <li>View own cards (with pagination)</li>
 *   <li>Get specific card details</li>
 *   <li>Check card balance</li>
 *   <li>Transfer money between own cards</li>
 * </ul>
 * <p>
 * Security: Users can only access their own cards. Card numbers are always
 * masked in responses (only last 4 digits visible).
 *
 * @see CardService
 * @see CardResponse
 * @see TransferRequest
 */
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;


    /**
     * Retrieves all cards belonging to the authenticated user.
     * <p>
     * Returns a paginated list of the user's cards with masked card numbers.
     * <p>
     * <strong>Endpoint:</strong> GET /api/cards
     * <p>
     * <strong>Query parameters:</strong>
     * <ul>
     *   <li>page - Page number (default: 0)</li>
     *   <li>size - Page size (default: 20)</li>
     *   <li>sort - Sort criteria (e.g., "id,desc")</li>
     * </ul>
     * <p>
     * <strong>Example:</strong> GET /api/cards?page=0&size=10&sort=id,desc
     *
     * @param pageable pagination parameters
     * @return ResponseEntity with a page of CardResponse objects
     */
    @GetMapping
    public ResponseEntity<Page<CardResponse>> getMyCards(Pageable pageable) {
        return ResponseEntity.ok(cardService.getOwn(pageable));
    }

    /**
     * Retrieves details of a specific card owned by the authenticated user.
     * <p>
     * Returns card information with masked card number.
     * <p>
     * <strong>Endpoint:</strong> GET /api/cards/{id}
     *
     * @param id the ID of the card to retrieve
     * @return ResponseEntity with CardResponse containing card details
     * @throws com.example.bankcards.exception.NotFoundException if card doesn't exist or doesn't belong to user
     */
    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getMyCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getOwn(id));
    }

    /**
     * Retrieves the current balance of a card owned by the authenticated user.
     * <p>
     * <strong>Endpoint:</strong> GET /api/cards/balance/{id}
     *
     * @param id the ID of the card
     * @return ResponseEntity with the card's current balance
     * @throws com.example.bankcards.exception.NotFoundException if card doesn't exist
     * @throws com.example.bankcards.exception.AccessDeniedException if card doesn't belong to user
     */
    @GetMapping("/balance/{id}")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getBalance(id));
    }

    /**
     * Transfers money between two cards owned by the authenticated user.
     * <p>
     * Both cards must belong to the current user and have ACTIVE status.
     * The source card must have sufficient balance.
     * <p>
     * <strong>Endpoint:</strong> POST /api/cards/transfer
     * <p>
     * <strong>Request body example:</strong>
     * <pre>
     * {
     *   "fromCardId": 1,
     *   "toCardId": 2,
     *   "amount": 100.00
     * }
     * </pre>
     *
     * @param request the transfer request containing source card, destination card, and amount
     * @return ResponseEntity with 200 OK status on success
     * @throws IllegalArgumentException if validation fails (same cards, invalid amount, inactive cards, insufficient funds)
     * @throws com.example.bankcards.exception.NotFoundException if either card doesn't exist
     * @throws com.example.bankcards.exception.AccessDeniedException if either card doesn't belong to user
     */
    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        cardService.transfer(request);
        return ResponseEntity.ok().build();
    }
}



