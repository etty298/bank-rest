package com.example.bankcards.service;

import com.example.bankcards.dto.auth.JwtResponse;
import com.example.bankcards.dto.auth.LoginRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AuthService - Comprehensive Tests")
class AuthServiceTest {

    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private UserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setup() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        userRepository = mock(UserRepository.class);
        authService = new AuthService(authenticationManager, jwtTokenProvider, userRepository);
    }

    @Test
    @DisplayName("login() should authenticate user and return JWT token")
    void loginShouldAuthenticateAndReturnToken() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        Authentication authentication = mock(Authentication.class);
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .role(Role.USER)
                .enabled(true)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken("testuser", "USER")).thenReturn("jwt-token-123");

        // When
        JwtResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token-123", response.token());
        assertEquals("testuser", response.username());
        assertEquals("USER", response.role());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken("testuser", "USER");
    }

    @Test
    @DisplayName("login() should work for ADMIN role")
    void loginShouldWorkForAdmin() {
        // Given
        LoginRequest request = new LoginRequest("admin", "adminpass");
        Authentication authentication = mock(Authentication.class);
        User user = User.builder()
                .id(2L)
                .username("admin")
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken("admin", "ADMIN")).thenReturn("admin-jwt-token");

        // When
        JwtResponse response = authService.login(request);

        // Then
        assertEquals("admin-jwt-token", response.token());
        assertEquals("admin", response.username());
        assertEquals("ADMIN", response.role());
    }

    @Test
    @DisplayName("login() should throw exception when authentication fails")
    void loginShouldThrowExceptionWhenAuthenticationFails() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When/Then
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("login() should throw exception when user not found after authentication")
    void loginShouldThrowExceptionWhenUserNotFound() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When/Then
        assertThrows(Exception.class, () -> authService.login(request));
    }
}
