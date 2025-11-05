package com.example.bankcards.dto.card;

import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse(
        Long id,
        String maskedCardNumber,
        LocalDate expirationDate,
        CardStatus status,
        BigDecimal balance
) {}



