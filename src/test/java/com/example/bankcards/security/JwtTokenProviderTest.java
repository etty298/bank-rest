package com.example.bankcards.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    @Test
    void generatesAndValidatesToken() {
        JwtTokenProvider provider = new JwtTokenProvider("test-secret-12345678901234567890", 5);
        String token = provider.generateToken("user", "USER");
        assertTrue(provider.validate(token));
        assertEquals("user", provider.getUsername(token));
        assertEquals("USER", provider.getRole(token));
    }
}



