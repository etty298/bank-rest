package com.example.bankcards.controller;

import com.example.bankcards.dto.auth.JwtResponse;
import com.example.bankcards.dto.auth.LoginRequest;
import com.example.bankcards.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthController - Comprehensive Tests")
class AuthControllerTest {

    private AuthService authService;
    private AuthController authController;

    @BeforeEach
    void setup() {
        authService = mock(AuthService.class);
        authController = new AuthController(authService);
    }

    @Test
    @DisplayName("login() should return JwtResponse with OK status")
    void loginShouldReturnJwtResponseWithOkStatus() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        JwtResponse jwtResponse = new JwtResponse("jwt-token-123", "testuser", "USER");
        when(authService.login(request)).thenReturn(jwtResponse);

        // When
        ResponseEntity<JwtResponse> response = authController.login(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("jwt-token-123", response.getBody().token());
        assertEquals("testuser", response.getBody().username());
        assertEquals("USER", response.getBody().role());
        verify(authService).login(request);
    }

    @Test
    @DisplayName("login() should call authService.login()")
    void loginShouldCallAuthService() {
        // Given
        LoginRequest request = new LoginRequest("admin", "adminpass");
        JwtResponse jwtResponse = new JwtResponse("admin-token", "admin", "ADMIN");
        when(authService.login(request)).thenReturn(jwtResponse);

        // When
        authController.login(request);

        // Then
        verify(authService).login(request);
    }

    @Test
    @DisplayName("login() should return response with admin role")
    void loginShouldReturnAdminRole() {
        // Given
        LoginRequest request = new LoginRequest("admin", "adminpass");
        JwtResponse jwtResponse = new JwtResponse("admin-token", "admin", "ADMIN");
        when(authService.login(request)).thenReturn(jwtResponse);

        // When
        ResponseEntity<JwtResponse> response = authController.login(request);

        // Then
        assertEquals("ADMIN", response.getBody().role());
    }
}
