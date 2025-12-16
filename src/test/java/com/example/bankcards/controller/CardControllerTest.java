package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.TransferRequest;
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

@DisplayName("CardController - Comprehensive Tests")
class CardControllerTest {

    private CardService cardService;
    private CardController cardController;

    @BeforeEach
    void setup() {
        cardService = mock(CardService.class);
        cardController = new CardController(cardService);
    }

    @Test
    @DisplayName("getMyCards() should return page of cards")
    void getMyCardsShouldReturnPageOfCards() {
        // Given
        CardResponse card1 = new CardResponse(1L, "**** **** **** 1234", 
                LocalDate.of(2025, 12, 31), CardStatus.ACTIVE, new BigDecimal("100.00"));
        CardResponse card2 = new CardResponse(2L, "**** **** **** 5678", 
                LocalDate.of(2026, 6, 30), CardStatus.BLOCKED, new BigDecimal("200.00"));
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardResponse> cardPage = new PageImpl<>(List.of(card1, card2));
        when(cardService.getOwn(pageable)).thenReturn(cardPage);

        // When
        ResponseEntity<Page<CardResponse>> response = cardController.getMyCards(pageable);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        verify(cardService).getOwn(pageable);
    }

    @Test
    @DisplayName("getMyCard() should return single card")
    void getMyCardShouldReturnSingleCard() {
        // Given
        CardResponse cardResponse = new CardResponse(1L, "**** **** **** 1234", 
                LocalDate.of(2025, 12, 31), CardStatus.ACTIVE, new BigDecimal("100.00"));
        when(cardService.getOwn(1L)).thenReturn(cardResponse);

        // When
        ResponseEntity<CardResponse> response = cardController.getMyCard(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("**** **** **** 1234", response.getBody().maskedCardNumber());
        verify(cardService).getOwn(1L);
    }

    @Test
    @DisplayName("getBalance() should return card balance")
    void getBalanceShouldReturnCardBalance() {
        // Given
        BigDecimal balance = new BigDecimal("150.50");
        when(cardService.getBalance(1L)).thenReturn(balance);

        // When
        ResponseEntity<BigDecimal> response = cardController.getBalance(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(new BigDecimal("150.50"), response.getBody());
        verify(cardService).getBalance(1L);
    }

    @Test
    @DisplayName("transfer() should call cardService and return OK")
    void transferShouldCallServiceAndReturnOk() {
        // Given
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("50.00"));
        doNothing().when(cardService).transfer(request);

        // When
        ResponseEntity<Void> response = cardController.transfer(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(cardService).transfer(request);
    }

    @Test
    @DisplayName("getMyCards() should handle empty result")
    void getMyCardsShouldHandleEmptyResult() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardResponse> emptyPage = new PageImpl<>(List.of());
        when(cardService.getOwn(pageable)).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<CardResponse>> response = cardController.getMyCards(pageable);

        // Then
        assertEquals(0, response.getBody().getTotalElements());
    }
}
