package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CreateCardRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AdminCardController - Comprehensive Tests")
class AdminCardControllerTest {

    private CardService cardService;
    private AdminCardController adminCardController;

    @BeforeEach
    void setup() {
        cardService = mock(CardService.class);
        adminCardController = new AdminCardController(cardService);
    }

    @Test
    @DisplayName("create() should create card and return response with OK status")
    void createShouldCreateCardAndReturnOkStatus() {
        // Given
        CreateCardRequest request = new CreateCardRequest(1L, "1234567890123456", LocalDate.of(2025, 12, 31));
        CardResponse cardResponse = new CardResponse(1L, "**** **** **** 3456", 
                LocalDate.of(2025, 12, 31), CardStatus.BLOCKED, BigDecimal.ZERO);
        when(cardService.createForUser(request)).thenReturn(cardResponse);

        // When
        ResponseEntity<CardResponse> response = adminCardController.create(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals(CardStatus.BLOCKED, response.getBody().status());
        assertEquals(BigDecimal.ZERO, response.getBody().balance());
        verify(cardService).createForUser(request);
    }

    @Test
    @DisplayName("activate() should activate card and return response")
    void activateShouldActivateCard() {
        // Given
        CardResponse cardResponse = new CardResponse(1L, "**** **** **** 3456", 
                LocalDate.of(2025, 12, 31), CardStatus.ACTIVE, new BigDecimal("100.00"));
        when(cardService.activate(1L)).thenReturn(cardResponse);

        // When
        ResponseEntity<CardResponse> response = adminCardController.activate(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(CardStatus.ACTIVE, response.getBody().status());
        verify(cardService).activate(1L);
    }

    @Test
    @DisplayName("block() should block card and return response")
    void blockShouldBlockCard() {
        // Given
        CardResponse cardResponse = new CardResponse(1L, "**** **** **** 3456", 
                LocalDate.of(2025, 12, 31), CardStatus.BLOCKED, new BigDecimal("100.00"));
        when(cardService.block(1L)).thenReturn(cardResponse);

        // When
        ResponseEntity<CardResponse> response = adminCardController.block(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(CardStatus.BLOCKED, response.getBody().status());
        verify(cardService).block(1L);
    }

    @Test
    @DisplayName("delete() should call cardService.delete and return NO_CONTENT status")
    void deleteShouldCallServiceAndReturnNoContent() {
        // Given
        doNothing().when(cardService).delete(1L);

        // When
        ResponseEntity<Void> response = adminCardController.delete(1L);

        // Then
        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(cardService).delete(1L);
    }

    @Test
    @DisplayName("getAll() should return page of all cards")
    void getAllShouldReturnPageOfAllCards() {
        // Given
        CardResponse card1 = new CardResponse(1L, "**** **** **** 1234", 
                LocalDate.of(2025, 12, 31), CardStatus.ACTIVE, new BigDecimal("100.00"));
        CardResponse card2 = new CardResponse(2L, "**** **** **** 5678", 
                LocalDate.of(2026, 6, 30), CardStatus.BLOCKED, new BigDecimal("200.00"));
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardResponse> cardPage = new PageImpl<>(List.of(card1, card2));
        when(cardService.getAll(pageable)).thenReturn(cardPage);

        // When
        ResponseEntity<Page<CardResponse>> response = adminCardController.getAll(pageable);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().getTotalElements());
        verify(cardService).getAll(pageable);
    }

    @Test
    @DisplayName("getAll() should handle empty result")
    void getAllShouldHandleEmptyResult() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardResponse> emptyPage = new PageImpl<>(List.of());
        when(cardService.getAll(pageable)).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<CardResponse>> response = adminCardController.getAll(pageable);

        // Then
        assertEquals(0, response.getBody().getTotalElements());
    }
}
