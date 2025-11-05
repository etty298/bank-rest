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
import com.example.bankcards.util.MaskingUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CryptoUtil cryptoUtil;

    public CardService(CardRepository cardRepository, UserRepository userRepository, CryptoUtil cryptoUtil) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cryptoUtil = cryptoUtil;
    }

    public CardResponse getOwn(Long id) {
        User current = getCurrentUser();
        Card card = cardRepository.findByIdAndOwnerId(id, current.getId())
                .orElseThrow(() -> new NotFoundException("Card not found: " + id));
        return toResponse(card);
    }

    public Page<CardResponse> getOwn(Pageable pageable) {
        User current = getCurrentUser();
        return cardRepository.findAllByOwner(current, pageable).map(this::toResponse);
    }

    public BigDecimal getBalance(Long id) {
        return getOwnEntity(id).getBalance();
    }

    @Transactional
    public void transfer(TransferRequest request) {
        if (request.fromCardId().equals(request.toCardId())) {
            throw new IllegalArgumentException("fromCardId must not equal toCardId");
        }
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Card from = getOwnEntity(request.fromCardId());
        Card to = getOwnEntity(request.toCardId());
        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Both cards must be ACTIVE");
        }
        if (from.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        from.setBalance(from.getBalance().subtract(request.amount()));
        to.setBalance(to.getBalance().add(request.amount()));
        cardRepository.save(from);
        cardRepository.save(to);
    }

    // ADMIN
    public CardResponse createForUser(CreateCardRequest request) {
        User user = userRepository.findById(request.userId()).orElseThrow(() -> new NotFoundException("User not found: " + request.userId()));
        String encrypted = cryptoUtil.encrypt(request.cardNumber());
        Card card = Card.builder()
                .encryptedCardNumber(encrypted)
                .owner(user)
                .expirationDate(request.expirationDate())
                .status(CardStatus.BLOCKED)
                .balance(BigDecimal.ZERO)
                .build();
        card = cardRepository.save(card);
        return toResponse(card);
    }

    public CardResponse activate(Long id) {
        Card card = getById(id);
        card.setStatus(CardStatus.ACTIVE);
        return toResponse(cardRepository.save(card));
    }

    public CardResponse block(Long id) {
        Card card = getById(id);
        card.setStatus(CardStatus.BLOCKED);
        return toResponse(cardRepository.save(card));
    }

    public void delete(Long id) { cardRepository.deleteById(id); }

    public Page<CardResponse> getAll(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::toResponse);
    }

    private Card getById(Long id) {
        return cardRepository.findById(id).orElseThrow(() -> new NotFoundException("Card not found: " + id));
    }

    private Card getOwnEntity(Long id) {
        User current = getCurrentUser();
        Card card = cardRepository.findById(id).orElseThrow(() -> new NotFoundException("Card not found: " + id));
        if (!card.getOwner().getId().equals(current.getId())) {
            throw new AccessDeniedException("You don't own this card");
        }
        return card;
    }

    private CardResponse toResponse(Card card) {
        String number = cryptoUtil.decrypt(card.getEncryptedCardNumber());
        return new CardResponse(card.getId(), MaskingUtil.maskCardNumber(number), card.getExpirationDate(), card.getStatus(), card.getBalance());
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username).orElseThrow();
    }
}



