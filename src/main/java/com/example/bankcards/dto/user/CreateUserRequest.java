package com.example.bankcards.dto.user;

import com.example.bankcards.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotNull Role role
) {}



