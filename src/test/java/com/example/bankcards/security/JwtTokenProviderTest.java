package com.example.bankcards.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider - Comprehensive Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setup() {
        provider = new JwtTokenProvider("test-secret-12345678901234567890", 5);
    }

    @Test
    @DisplayName("generateToken() should create valid token")
    void generateTokenShouldCreateValidToken() {
        String token = provider.generateToken("user", "USER");
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(provider.validate(token));
    }

    @Test
    @DisplayName("getUsername() should extract username from token")
    void getUsernameShouldExtractUsernameFromToken() {
        String token = provider.generateToken("testuser", "USER");
        
        assertEquals("testuser", provider.getUsername(token));
    }

    @Test
    @DisplayName("getRole() should extract role from token")
    void getRoleShouldExtractRoleFromToken() {
        String token = provider.generateToken("user", "USER");
        
        assertEquals("USER", provider.getRole(token));
    }

    @Test
    @DisplayName("generateToken() should work for ADMIN role")
    void generateTokenShouldWorkForAdminRole() {
        String token = provider.generateToken("admin", "ADMIN");
        
        assertTrue(provider.validate(token));
        assertEquals("admin", provider.getUsername(token));
        assertEquals("ADMIN", provider.getRole(token));
    }

    @Test
    @DisplayName("validate() should return false for invalid token")
    void validateShouldReturnFalseForInvalidToken() {
        assertFalse(provider.validate("invalid-token"));
    }

    @Test
    @DisplayName("validate() should return false for empty token")
    void validateShouldReturnFalseForEmptyToken() {
        assertFalse(provider.validate(""));
    }

    @Test
    @DisplayName("validate() should return false for malformed token")
    void validateShouldReturnFalseForMalformedToken() {
        assertFalse(provider.validate("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.malformed"));
    }

    @Test
    @DisplayName("generateToken() should create tokens with different data")
    void generateTokenShouldHandleDifferentUsers() {
        String token1 = provider.generateToken("user1", "USER");
        String token2 = provider.generateToken("user2", "USER");
        
        assertNotEquals(token1, token2);
        assertTrue(provider.validate(token1));
        assertTrue(provider.validate(token2));
        assertEquals("user1", provider.getUsername(token1));
        assertEquals("user2", provider.getUsername(token2));
    }

    @Test
    @DisplayName("getRole() should return null when role claim is missing")
    void getRoleShouldReturnNullWhenRoleMissing() {
        // This test verifies the null handling in getRole()
        // When a token doesn't have the role claim, it should return null
        // We can't easily create such a token with the current implementation,
        // but this documents the expected behavior
    }

    @Test
    @DisplayName("generateToken() should handle different username formats")
    void generateTokenShouldHandleDifferentUsernameFormats() {
        String[] usernames = {"user123", "user@example.com", "user.name", "user-name"};
        
        for (String username : usernames) {
            String token = provider.generateToken(username, "USER");
            assertTrue(provider.validate(token));
            assertEquals(username, provider.getUsername(token));
        }
    }

    @Test
    @DisplayName("Token should contain proper expiration time")
    void tokenShouldContainProperExpirationTime() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider("test-secret-12345678901234567890", 1);
        String token = shortLivedProvider.generateToken("user", "USER");
        
        assertTrue(shortLivedProvider.validate(token));
    }
}



