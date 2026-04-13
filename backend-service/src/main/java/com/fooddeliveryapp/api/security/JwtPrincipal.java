package com.fooddeliveryapp.api.security;

import com.fooddeliveryapp.api.models.Role;

public record JwtPrincipal(Long userId, Role role) {
}

