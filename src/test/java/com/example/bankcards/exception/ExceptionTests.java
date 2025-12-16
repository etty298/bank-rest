package com.example.bankcards.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Exception Classes - Comprehensive Tests")
class ExceptionTests {

    @Test
    @DisplayName("NotFoundException should contain message")
    void notFoundExceptionShouldContainMessage() {
        String message = "User not found: 123";
        NotFoundException exception = new NotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("NotFoundException should be a RuntimeException")
    void notFoundExceptionShouldBeRuntimeException() {
        NotFoundException exception = new NotFoundException("test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("AccessDeniedException should contain message")
    void accessDeniedExceptionShouldContainMessage() {
        String message = "You don't own this card";
        AccessDeniedException exception = new AccessDeniedException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("AccessDeniedException should be a RuntimeException")
    void accessDeniedExceptionShouldBeRuntimeException() {
        AccessDeniedException exception = new AccessDeniedException("test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("NotFoundException can be thrown and caught")
    void notFoundExceptionCanBeThrownAndCaught() {
        assertThrows(NotFoundException.class, () -> {
            throw new NotFoundException("Resource not found");
        });
    }

    @Test
    @DisplayName("AccessDeniedException can be thrown and caught")
    void accessDeniedExceptionCanBeThrownAndCaught() {
        assertThrows(AccessDeniedException.class, () -> {
            throw new AccessDeniedException("Access denied");
        });
    }
}
