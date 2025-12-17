package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility component for encrypting and decrypting sensitive data using AES-256 encryption.
 * <p>
 * This component is used to encrypt bank card numbers before storing them in the database
 * and decrypt them when needed for processing. The encryption uses:
 * <ul>
 *   <li>AES (Advanced Encryption Standard) algorithm</li>
 *   <li>CBC (Cipher Block Chaining) mode</li>
 *   <li>PKCS5Padding for padding</li>
 *   <li>Randomly generated IV (Initialization Vector) for each encryption</li>
 * </ul>
 * <p>
 * The encryption key is configured via the {@code app.crypto.aesSecret} property
 * and should be 32 bytes (256 bits) for AES-256 encryption.
 * <p>
 * The encrypted output is Base64-encoded and includes both the IV and ciphertext.
 * Format: [IV (16 bytes)][Ciphertext (variable length)]
 *
 * @see com.example.bankcards.entity.Card
 * @see com.example.bankcards.service.CardService
 */
@Component
public class CryptoUtil {

    private final byte[] key;

    /**
     * Constructs a CryptoUtil instance with the specified AES secret key.
     * <p>
     * The secret key is read from the application configuration property
     * {@code app.crypto.aesSecret}. The key should be 32 bytes (256 bits) long
     * for AES-256 encryption.
     *
     * @param aesSecret the AES secret key from application configuration
     */
    public CryptoUtil(@Value("${app.crypto.aesSecret}") String aesSecret) {
        this.key = aesSecret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Encrypts the given plain text using AES-256 encryption.
     * <p>
     * This method generates a random 16-byte IV for each encryption operation,
     * encrypts the plain text, and returns a Base64-encoded string containing
     * both the IV and the ciphertext.
     * <p>
     * The output format is: Base64([IV (16 bytes)][Ciphertext])
     *
     * @param plainText the plain text to encrypt (e.g., card number)
     * @return Base64-encoded string containing IV and encrypted data
     * @throws IllegalStateException if encryption fails due to cryptographic errors
     */
    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] ivPlusCipher = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, ivPlusCipher, 0, iv.length);
            System.arraycopy(encrypted, 0, ivPlusCipher, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(ivPlusCipher);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption error", e);
        }
    }

    /**
     * Decrypts the given cipher text using AES-256 decryption.
     * <p>
     * This method expects the cipher text to be a Base64-encoded string
     * containing both the IV and the encrypted data in the format produced
     * by the {@link #encrypt(String)} method.
     * <p>
     * The method extracts the IV from the first 16 bytes and uses it to
     * decrypt the remaining ciphertext.
     * <p>
     * <strong>Note:</strong> If decryption fails (e.g., for legacy demo data
     * that was not encrypted), this method returns the original cipher text
     * as a fallback.
     *
     * @param cipherText the Base64-encoded cipher text to decrypt
     * @return the decrypted plain text, or the original cipher text if decryption fails
     */
    public String decrypt(String cipherText) {
        try {
            byte[] ivPlusCipher = Base64.getDecoder().decode(cipherText);
            byte[] iv = new byte[16];
            byte[] encrypted = new byte[ivPlusCipher.length - 16];
            System.arraycopy(ivPlusCipher, 0, iv, 0, 16);
            System.arraycopy(ivPlusCipher, 16, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
            byte[] plain = cipher.doFinal(encrypted);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // For demo data that may not be encrypted, just return the original string
            return cipherText;
        }
    }
}


