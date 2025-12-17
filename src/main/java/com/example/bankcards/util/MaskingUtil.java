package com.example.bankcards.util;

/**
 * Utility class for masking sensitive data such as card numbers.
 * <p>
 * This class provides static methods to mask sensitive information
 * for display purposes, ensuring that full card numbers are never
 * exposed to users or in logs.
 * <p>
 * The masking follows PCI DSS (Payment Card Industry Data Security Standard)
 * guidelines by displaying only the last 4 digits of card numbers.
 * <p>
 * This is a utility class with only static methods and cannot be instantiated.
 *
 * @see com.example.bankcards.dto.card.CardResponse
 * @see com.example.bankcards.service.CardService
 */
public final class MaskingUtil {
    
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MaskingUtil() {}

    /**
     * Masks a card number by replacing all but the last 4 digits with asterisks.
     * <p>
     * This method formats the masked card number in the pattern:
     * {@code **** **** **** 1234}
     * <p>
     * Examples:
     * <ul>
     *   <li>Input: "1234567890123456" → Output: "**** **** **** 3456"</li>
     *   <li>Input: "1234" → Output: "**** **** **** 1234"</li>
     *   <li>Input: "12" → Output: "****" (too short)</li>
     *   <li>Input: null → Output: "****"</li>
     * </ul>
     * <p>
     * If the input is null or has fewer than 4 characters, returns "****".
     *
     * @param cardNumber the card number to mask (can be null)
     * @return the masked card number showing only the last 4 digits, or "****" if invalid
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + last4;
    }
}



