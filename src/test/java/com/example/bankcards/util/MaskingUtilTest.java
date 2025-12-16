package com.example.bankcards.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MaskingUtil - Comprehensive Tests")
class MaskingUtilTest {

    @Test
    @DisplayName("maskCardNumber() should mask 16-digit card number correctly")
    void maskCardNumberShouldMask16DigitCard() {
        String result = MaskingUtil.maskCardNumber("1234567890123456");
        assertEquals("**** **** **** 3456", result);
    }

    @Test
    @DisplayName("maskCardNumber() should mask 13-digit card number correctly")
    void maskCardNumberShouldMask13DigitCard() {
        String result = MaskingUtil.maskCardNumber("1234567890123");
        assertEquals("**** **** **** 0123", result);
    }

    @Test
    @DisplayName("maskCardNumber() should return **** when card number is null")
    void maskCardNumberShouldReturnStarsWhenNull() {
        String result = MaskingUtil.maskCardNumber(null);
        assertEquals("****", result);
    }

    @Test
    @DisplayName("maskCardNumber() should return **** when card number is less than 4 characters")
    void maskCardNumberShouldReturnStarsWhenTooShort() {
        assertEquals("****", MaskingUtil.maskCardNumber("123"));
        assertEquals("****", MaskingUtil.maskCardNumber("12"));
        assertEquals("****", MaskingUtil.maskCardNumber("1"));
        assertEquals("****", MaskingUtil.maskCardNumber(""));
    }

    @Test
    @DisplayName("maskCardNumber() should handle exactly 4 characters")
    void maskCardNumberShouldHandleExactly4Characters() {
        String result = MaskingUtil.maskCardNumber("1234");
        assertEquals("**** **** **** 1234", result);
    }

    @Test
    @DisplayName("maskCardNumber() should handle 19-digit card number")
    void maskCardNumberShouldHandle19DigitCard() {
        String result = MaskingUtil.maskCardNumber("1234567890123456789");
        assertEquals("**** **** **** 6789", result);
    }
}
