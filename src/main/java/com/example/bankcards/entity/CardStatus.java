package com.example.bankcards.entity;

/**
 * Enumeration representing the status of a bank card.
 * <p>
 * This enum defines the lifecycle states of a card in the system:
 * <ul>
 *   <li>{@link #ACTIVE} - Card is active and can be used for transactions</li>
 *   <li>{@link #BLOCKED} - Card is blocked and cannot be used</li>
 *   <li>{@link #EXPIRED} - Card has passed its expiration date</li>
 * </ul>
 * <p>
 * Card status affects whether operations can be performed on the card.
 * Only ACTIVE cards can participate in transfers.
 *
 * @see Card
 */
public enum CardStatus {
    /**
     * Card is active and can be used for all operations.
     * <p>
     * Active cards can:
     * <ul>
     *   <li>Be viewed by the owner</li>
     *   <li>Participate in transfers (send and receive funds)</li>
     *   <li>Be blocked by administrators</li>
     * </ul>
     */
    ACTIVE,
    
    /**
     * Card is blocked and cannot be used for transactions.
     * <p>
     * Blocked cards:
     * <ul>
     *   <li>Can be viewed by the owner</li>
     *   <li>Cannot participate in transfers</li>
     *   <li>Can be activated by administrators</li>
     * </ul>
     */
    BLOCKED,
    
    /**
     * Card has expired based on its expiration date.
     * <p>
     * Expired cards:
     * <ul>
     *   <li>Can be viewed by the owner</li>
     *   <li>Cannot participate in transfers</li>
     *   <li>Cannot be reactivated</li>
     * </ul>
     */
    EXPIRED
}



