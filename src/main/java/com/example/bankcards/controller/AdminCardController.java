package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CreateCardRequest;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for administrative card operations.
 * <p>
 * This controller provides endpoints for administrators to manage all cards
 * in the system. All endpoints require ADMIN role authentication.
 * <p>
 * Base path: {@code /api/admin/cards}
 * <p>
 * Administrative operations:
 * <ul>
 *   <li>Create new cards for users</li>
 *   <li>Activate blocked cards</li>
 *   <li>Block active cards</li>
 *   <li>Delete cards</li>
 *   <li>View all cards in the system</li>
 * </ul>
 * <p>
 * Security: All endpoints are protected by {@code @PreAuthorize("hasRole('ADMIN')")}
 * and require a valid JWT token with ADMIN role.
 *
 * @see CardService
 * @see CardResponse
 * @see CreateCardRequest
 */
@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCardController {
    private final CardService cardService;


    /**
     * Creates a new card for a specified user.
     * <p>
     * The card is created with BLOCKED status and zero balance.
     * The card number is encrypted before storage.
     * <p>
     * <strong>Endpoint:</strong> POST /api/admin/cards
     * <p>
     * <strong>Request body example:</strong>
     * <pre>
     * {
     *   "userId": 1,
     *   "cardNumber": "1234567890123456",
     *   "expirationDate": "2025-12-31"
     * }
     * </pre>
     *
     * @param request the card creation request containing user ID, card number, and expiration date
     * @return ResponseEntity with CardResponse containing the created card details
     * @throws com.example.bankcards.exception.NotFoundException if the specified user doesn't exist
     */
    @PostMapping
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CreateCardRequest request) {
        return ResponseEntity.ok(cardService.createForUser(request));
    }

    /**
     * Activates a blocked card.
     * <p>
     * Changes the card status from BLOCKED to ACTIVE, allowing transactions.
     * <p>
     * <strong>Endpoint:</strong> PATCH /api/admin/cards/{id}/activate
     *
     * @param id the ID of the card to activate
     * @return ResponseEntity with CardResponse containing updated card details
     * @throws com.example.bankcards.exception.NotFoundException if the card doesn't exist
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<CardResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.activate(id));
    }

    /**
     * Blocks an active card.
     * <p>
     * Changes the card status to BLOCKED, preventing it from being used for transactions.
     * This is typically used for security purposes or account suspension.
     * <p>
     * <strong>Endpoint:</strong> PATCH /api/admin/cards/{id}/block
     *
     * @param id the ID of the card to block
     * @return ResponseEntity with CardResponse containing updated card details
     * @throws com.example.bankcards.exception.NotFoundException if the card doesn't exist
     */
    @PatchMapping("/{id}/block")
    public ResponseEntity<CardResponse> block(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.block(id));
    }

    /**
     * Permanently deletes a card from the system.
     * <p>
     * This operation cannot be undone. All card data will be permanently removed.
     * <p>
     * <strong>Endpoint:</strong> DELETE /api/admin/cards/{id}
     *
     * @param id the ID of the card to delete
     * @return ResponseEntity with 204 No Content status on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all cards in the system with pagination.
     * <p>
     * Returns a paginated list of all cards regardless of owner.
     * Card numbers are masked in responses.
     * <p>
     * <strong>Endpoint:</strong> GET /api/admin/cards
     * <p>
     * <strong>Query parameters:</strong>
     * <ul>
     *   <li>page - Page number (default: 0)</li>
     *   <li>size - Page size (default: 20)</li>
     *   <li>sort - Sort criteria (e.g., "id,desc")</li>
     * </ul>
     *
     * @param pageable pagination parameters
     * @return ResponseEntity with a page of CardResponse objects
     */
    @GetMapping
    public ResponseEntity<Page<CardResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(cardService.getAll(pageable));
    }
}



