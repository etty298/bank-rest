package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CryptoUtil {

    private final byte[] key;

    public CryptoUtil(@Value("${app.crypto.aesSecret}") String aesSecret) {
        this.key = aesSecret.getBytes(StandardCharsets.UTF_8);
    }

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


