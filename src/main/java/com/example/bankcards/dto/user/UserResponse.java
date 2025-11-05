package com.example.bankcards.dto.user;

import com.example.bankcards.entity.Role;

public record UserResponse(Long id, String username, Role role, boolean enabled) {}



