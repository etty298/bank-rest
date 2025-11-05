package com.example.bankcards.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void handlesNotFound() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<?> response = handler.handleNotFound(new NotFoundException("x"));
        assertEquals(404, response.getStatusCode().value());
    }
}



