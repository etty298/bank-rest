package com.example.bankcards.dto.card;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull Long fromCardId,
        @NotNull Long toCardId,
        @NotNull @Min(1) BigDecimal amount
) {}



