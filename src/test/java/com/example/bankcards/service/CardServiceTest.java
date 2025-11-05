package com.example.bankcards.service;

import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.entity.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CryptoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CardServiceTest {

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private CryptoUtil cryptoUtil;
    private CardService cardService;

    @BeforeEach
    void setup() {
        cardRepository = Mockito.mock(CardRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        cryptoUtil = Mockito.mock(CryptoUtil.class);
        cardService = new CardService(cardRepository, userRepository, cryptoUtil);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user1", null));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(User.builder().id(1L).username("user1").role(Role.USER).enabled(true).build()));
    }

    @Test
    void transferMovesBalanceBetweenOwnCards() {
        Card from = Card.builder().id(10L).owner(User.builder().id(1L).build()).status(CardStatus.ACTIVE).balance(new BigDecimal("100.00")).expirationDate(LocalDate.now().plusYears(1)).encryptedCardNumber("x").build();
        Card to = Card.builder().id(11L).owner(User.builder().id(1L).build()).status(CardStatus.ACTIVE).balance(new BigDecimal("20.00")).expirationDate(LocalDate.now().plusYears(1)).encryptedCardNumber("y").build();
        when(cardRepository.findById(10L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(11L)).thenReturn(Optional.of(to));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cardService.transfer(new TransferRequest(10L, 11L, new BigDecimal("30.00")));

        assertEquals(new BigDecimal("70.00"), from.getBalance());
        assertEquals(new BigDecimal("50.00"), to.getBalance());
    }

}



