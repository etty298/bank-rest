package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateCardRequest(
        @NotNull Long userId,
        @NotBlank String cardNumber,
        @NotNull LocalDate expirationDate
) {}



