package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CreateCardRequest;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.entity.*;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CryptoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("CardService - Comprehensive Tests")
class CardServiceTest {

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private CryptoUtil cryptoUtil;
    private CardService cardService;
    private User currentUser;

    @BeforeEach
    void setup() {
        cardRepository = Mockito.mock(CardRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        cryptoUtil = Mockito.mock(CryptoUtil.class);
        cardService = new CardService(cardRepository, userRepository, cryptoUtil);
        
        currentUser = User.builder().id(1L).username("user1").role(Role.USER).enabled(true).build();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user1", null));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(currentUser));
    }

    @Test
    @DisplayName("transfer() should move balance between own cards")
    void transferMovesBalanceBetweenOwnCards() {
        Card from = Card.builder().id(10L).owner(User.builder().id(1L).build()).status(CardStatus.ACTIVE).balance(new BigDecimal("100.00")).expirationDate(LocalDate.now().plusYears(1)).encryptedCardNumber("x").build();
        Card to = Card.builder().id(11L).owner(User.builder().id(1L).build()).status(CardStatus.ACTIVE).balance(new BigDecimal("20.00")).expirationDate(LocalDate.now().plusYears(1)).encryptedCardNumber("y").build();
        when(cardRepository.findById(10L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(11L)).thenReturn(Optional.of(to));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cardService.transfer(new TransferRequest(10L, 11L, new BigDecimal("30.00")));

        assertEquals(new BigDecimal("70.00"), from.getBalance());
        assertEquals(new BigDecimal("50.00"), to.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    @DisplayName("transfer() should throw exception when fromCard equals toCard")
    void transferShouldThrowExceptionWhenSameCard() {
        TransferRequest request = new TransferRequest(10L, 10L, new BigDecimal("30.00"));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cardService.transfer(request));
        assertEquals("fromCardId must not equal toCardId", exception.getMessage());
    }

    @Test
    @DisplayName("transfer() should throw exception when amount is negative")
    void transferShouldThrowExceptionWhenAmountNegative() {
        TransferRequest request = new TransferRequest(10L, 11L, new BigDecimal("-10.00"));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cardService.transfer(request));
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("transfer() should throw exception when amount is zero")
    void transferShouldThrowExceptionWhenAmountZero() {
        TransferRequest request = new TransferRequest(10L, 11L, BigDecimal.ZERO);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cardService.transfer(request));
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("transfer() should throw exception when insufficient funds")
    void transferShouldThrowExceptionWhenInsufficientFunds() {
        Card from = Card.builder().id(10L).owner(User.builder().id(1L).build()).status(CardStatus.ACTIVE).balance(new BigDecimal("10.00")).encryptedCardNumber("x").build();
        Card to = Card.builder().id(11L).owner(User.builder().id(1L).build()).status(CardStatus.ACTIVE).balance(new BigDecimal("20.00")).encryptedCardNumber("y").build();
        when(cardRepository.findById(10L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(11L)).thenReturn(Optional.of(to));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cardService.transfer(new TransferRequest(10L, 11L, new BigDecimal("30.00"))));
        assertEquals("Insufficient funds", exception.getMessage());
    }

    @Test
    @DisplayName("transfer() should throw exception when fromCard is blocked")
    void transferShouldThrowExceptionWhenFromCardBlocked() {
        Card from = Card.builder().id(10L).owner(User.builder().id(1L).build()).status(CardStatus.BLOCKED).balance(new BigDecimal("100.00")).encryptedCardNumber("x").build();
        Card to = Card.builder().id(11L).owner(User.builder().id(1L).build()).status(CardStatus.ACTIVE).balance(new BigDecimal("20.00")).encryptedCardNumber("y").build();
        when(cardRepository.findById(10L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(11L)).thenReturn(Optional.of(to));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cardService.transfer(new TransferRequest(10L, 11L, new BigDecimal("30.00"))));
        assertEquals("Both cards must be ACTIVE", exception.getMessage());
    }

    @Test
    @DisplayName("transfer() should throw exception when toCard is blocked")
    void transferShouldThrowExceptionWhenToCardBlocked() {
        Card from = Card.builder().id(10L).owner(User.builder().id(1L).build()).status(CardStatus.ACTIVE).balance(new BigDecimal("100.00")).encryptedCardNumber("x").build();
        Card to = Card.builder().id(11L).owner(User.builder().id(1L).build()).status(CardStatus.BLOCKED).balance(new BigDecimal("20.00")).encryptedCardNumber("y").build();
        when(cardRepository.findById(10L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(11L)).thenReturn(Optional.of(to));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cardService.transfer(new TransferRequest(10L, 11L, new BigDecimal("30.00"))));
        assertEquals("Both cards must be ACTIVE", exception.getMessage());
    }

    @Test
    @DisplayName("getOwn(id) should return card when user owns it")
    void getOwnShouldReturnCardWhenOwned() {
        Card card = Card.builder()
                .id(10L)
                .owner(currentUser)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .expirationDate(LocalDate.of(2025, 12, 31))
                .encryptedCardNumber("encrypted123")
                .build();
        when(cardRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(card));
        when(cryptoUtil.decrypt("encrypted123")).thenReturn("1234567890123456");

        CardResponse response = cardService.getOwn(10L);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("**** **** **** 3456", response.maskedCardNumber());
        assertEquals(CardStatus.ACTIVE, response.status());
    }

    @Test
    @DisplayName("getOwn(id) should throw NotFoundException when card not found")
    void getOwnShouldThrowNotFoundExceptionWhenNotFound() {
        when(cardRepository.findByIdAndOwnerId(99L, 1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> cardService.getOwn(99L));
        assertEquals("Card not found: 99", exception.getMessage());
    }

    @Test
    @DisplayName("getOwn(pageable) should return page of user's cards")
    void getOwnShouldReturnPageOfCards() {
        Card card1 = Card.builder().id(1L).owner(currentUser).status(CardStatus.ACTIVE)
                .balance(new BigDecimal("100")).expirationDate(LocalDate.now())
                .encryptedCardNumber("enc1").build();
        Card card2 = Card.builder().id(2L).owner(currentUser).status(CardStatus.BLOCKED)
                .balance(new BigDecimal("200")).expirationDate(LocalDate.now())
                .encryptedCardNumber("enc2").build();
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(card1, card2));
        when(cardRepository.findAllByOwner(currentUser, pageable)).thenReturn(cardPage);
        when(cryptoUtil.decrypt(anyString())).thenReturn("1234567890123456");

        Page<CardResponse> responses = cardService.getOwn(pageable);

        assertEquals(2, responses.getTotalElements());
    }

    @Test
    @DisplayName("getBalance() should return balance of owned card")
    void getBalanceShouldReturnBalance() {
        Card card = Card.builder().id(10L).owner(currentUser).status(CardStatus.ACTIVE)
                .balance(new BigDecimal("150.50")).encryptedCardNumber("x").build();
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));

        BigDecimal balance = cardService.getBalance(10L);

        assertEquals(new BigDecimal("150.50"), balance);
    }

    @Test
    @DisplayName("getBalance() should throw AccessDeniedException when card not owned")
    void getBalanceShouldThrowAccessDeniedWhenNotOwned() {
        User otherUser = User.builder().id(2L).build();
        Card card = Card.builder().id(10L).owner(otherUser).status(CardStatus.ACTIVE)
                .balance(new BigDecimal("150.50")).encryptedCardNumber("x").build();
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
            () -> cardService.getBalance(10L));
        assertEquals("You don't own this card", exception.getMessage());
    }

    @Test
    @DisplayName("createForUser() should create card with BLOCKED status and zero balance")
    void createForUserShouldCreateBlockedCardWithZeroBalance() {
        User targetUser = User.builder().id(2L).username("targetuser").build();
        CreateCardRequest request = new CreateCardRequest(2L, "1234567890123456", LocalDate.of(2025, 12, 31));
        
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(cryptoUtil.encrypt("1234567890123456")).thenReturn("encrypted123");
        when(cryptoUtil.decrypt("encrypted123")).thenReturn("1234567890123456");
        
        Card savedCard = Card.builder()
                .id(10L)
                .encryptedCardNumber("encrypted123")
                .owner(targetUser)
                .expirationDate(LocalDate.of(2025, 12, 31))
                .status(CardStatus.BLOCKED)
                .balance(BigDecimal.ZERO)
                .build();
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        CardResponse response = cardService.createForUser(request);

        assertNotNull(response);
        assertEquals(CardStatus.BLOCKED, response.status());
        assertEquals(BigDecimal.ZERO, response.balance());
        verify(cryptoUtil).encrypt("1234567890123456");
    }

    @Test
    @DisplayName("createForUser() should throw NotFoundException when user not found")
    void createForUserShouldThrowNotFoundExceptionWhenUserNotFound() {
        CreateCardRequest request = new CreateCardRequest(99L, "1234567890123456", LocalDate.of(2025, 12, 31));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> cardService.createForUser(request));
        assertEquals("User not found: 99", exception.getMessage());
    }

    @Test
    @DisplayName("activate() should change card status to ACTIVE")
    void activateShouldChangeStatusToActive() {
        Card card = Card.builder().id(10L).status(CardStatus.BLOCKED)
                .encryptedCardNumber("enc").balance(BigDecimal.ZERO)
                .expirationDate(LocalDate.now()).build();
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cryptoUtil.decrypt(anyString())).thenReturn("1234567890123456");

        CardResponse response = cardService.activate(10L);

        assertEquals(CardStatus.ACTIVE, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("block() should change card status to BLOCKED")
    void blockShouldChangeStatusToBlocked() {
        Card card = Card.builder().id(10L).status(CardStatus.ACTIVE)
                .encryptedCardNumber("enc").balance(BigDecimal.ZERO)
                .expirationDate(LocalDate.now()).build();
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cryptoUtil.decrypt(anyString())).thenReturn("1234567890123456");

        CardResponse response = cardService.block(10L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("delete() should call repository deleteById")
    void deleteShouldCallRepositoryDeleteById() {
        cardService.delete(10L);
        verify(cardRepository).deleteById(10L);
    }

    @Test
    @DisplayName("getAll() should return page of all cards")
    void getAllShouldReturnPageOfAllCards() {
        Card card1 = Card.builder().id(1L).status(CardStatus.ACTIVE)
                .encryptedCardNumber("enc1").balance(BigDecimal.ZERO)
                .expirationDate(LocalDate.now()).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(card1));
        when(cardRepository.findAll(pageable)).thenReturn(cardPage);
        when(cryptoUtil.decrypt(anyString())).thenReturn("1234567890123456");

        Page<CardResponse> responses = cardService.getAll(pageable);

        assertEquals(1, responses.getTotalElements());
    }

}



