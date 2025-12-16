package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CryptoUtil - Comprehensive Tests")
class CryptoUtilTest {

    private CryptoUtil cryptoUtil;

    @BeforeEach
    void setup() {
        // Use a 32-byte key for AES-256
        String testSecret = "12345678901234567890123456789012";
        cryptoUtil = new CryptoUtil(testSecret);
    }

    @Test
    @DisplayName("encrypt() and decrypt() should work correctly")
    void encryptAndDecryptShouldWorkCorrectly() {
        String plainText = "1234567890123456";
        
        String encrypted = cryptoUtil.encrypt(plainText);
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        
        String decrypted = cryptoUtil.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("encrypt() should produce different outputs for same input (due to random IV)")
    void encryptShouldProduceDifferentOutputs() {
        String plainText = "1234567890123456";
        
        String encrypted1 = cryptoUtil.encrypt(plainText);
        String encrypted2 = cryptoUtil.encrypt(plainText);
        
        assertNotEquals(encrypted1, encrypted2, "Encryptions should differ due to random IV");
        
        assertEquals(plainText, cryptoUtil.decrypt(encrypted1));
        assertEquals(plainText, cryptoUtil.decrypt(encrypted2));
    }

    @Test
    @DisplayName("encrypt() and decrypt() should handle empty string")
    void encryptAndDecryptShouldHandleEmptyString() {
        String plainText = "";
        
        String encrypted = cryptoUtil.encrypt(plainText);
        String decrypted = cryptoUtil.decrypt(encrypted);
        
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("encrypt() and decrypt() should handle special characters")
    void encryptAndDecryptShouldHandleSpecialCharacters() {
        String plainText = "Test@#$%^&*()123!";
        
        String encrypted = cryptoUtil.encrypt(plainText);
        String decrypted = cryptoUtil.decrypt(encrypted);
        
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("decrypt() should return original string when decryption fails (for legacy data)")
    void decryptShouldReturnOriginalStringOnFailure() {
        String invalidCipherText = "not-a-valid-encrypted-string";
        
        String result = cryptoUtil.decrypt(invalidCipherText);
        
        assertEquals(invalidCipherText, result);
    }

    @Test
    @DisplayName("encrypt() and decrypt() should handle long strings")
    void encryptAndDecryptShouldHandleLongStrings() {
        String plainText = "1234567890".repeat(100); // 1000 characters
        
        String encrypted = cryptoUtil.encrypt(plainText);
        String decrypted = cryptoUtil.decrypt(encrypted);
        
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("encrypt() and decrypt() should handle Unicode characters")
    void encryptAndDecryptShouldHandleUnicode() {
        String plainText = "Hello 世界 🌍";
        
        String encrypted = cryptoUtil.encrypt(plainText);
        String decrypted = cryptoUtil.decrypt(encrypted);
        
        assertEquals(plainText, decrypted);
    }
}
