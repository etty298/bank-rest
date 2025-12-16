package com.example.bankcards.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler - Comprehensive Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleNotFound() should return 404 status")
    void handleNotFoundShouldReturn404() {
        ResponseEntity<?> response = handler.handleNotFound(new NotFoundException("User not found: 123"));
        
        assertEquals(404, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("NOT_FOUND", body.get("error"));
        assertEquals("User not found: 123", body.get("message"));
    }

    @Test
    @DisplayName("handleAccess() should return 403 status")
    void handleAccessShouldReturn403() {
        ResponseEntity<?> response = handler.handleAccess(new AccessDeniedException("You don't own this card"));
        
        assertEquals(403, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("FORBIDDEN", body.get("error"));
        assertEquals("You don't own this card", body.get("message"));
    }

    @Test
    @DisplayName("handleValidation() should return 400 status with field errors")
    void handleValidationShouldReturn400WithFieldErrors() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError1 = new FieldError("createUserRequest", "username", "must not be blank");
        FieldError fieldError2 = new FieldError("createUserRequest", "password", "must not be blank");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<?> response = handler.handleValidation(ex);
        
        // Then
        assertEquals(400, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("VALIDATION_ERROR", body.get("error"));
        List<String> messages = (List<String>) body.get("message");
        assertEquals(2, messages.size());
        assertTrue(messages.contains("username: must not be blank"));
        assertTrue(messages.contains("password: must not be blank"));
    }

    @Test
    @DisplayName("handleIllegal() should return 400 status")
    void handleIllegalShouldReturn400() {
        ResponseEntity<?> response = handler.handleIllegal(new IllegalArgumentException("Amount must be positive"));
        
        assertEquals(400, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("BAD_REQUEST", body.get("error"));
        assertEquals("Amount must be positive", body.get("message"));
    }

    @Test
    @DisplayName("handleBadCredentials() should return 401 status")
    void handleBadCredentialsShouldReturn401() {
        ResponseEntity<?> response = handler.handleBadCredentials(new BadCredentialsException("Bad credentials"));
        
        assertEquals(401, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("UNAUTHORIZED", body.get("error"));
        assertEquals("Неверные учётные данные", body.get("message"));
    }

    @Test
    @DisplayName("handleOther() should return 500 status for generic exceptions")
    void handleOtherShouldReturn500() {
        ResponseEntity<?> response = handler.handleOther(new RuntimeException("Unexpected error"));
        
        assertEquals(500, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("INTERNAL_ERROR", body.get("error"));
        assertEquals("Unexpected error", body.get("message"));
    }

    @Test
    @DisplayName("handleValidation() should handle empty field errors")
    void handleValidationShouldHandleEmptyFieldErrors() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        // When
        ResponseEntity<?> response = handler.handleValidation(ex);
        
        // Then
        assertEquals(400, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        List<String> messages = (List<String>) body.get("message");
        assertTrue(messages.isEmpty());
    }
}



