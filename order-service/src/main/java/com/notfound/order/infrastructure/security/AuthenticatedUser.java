package com.notfound.order.infrastructure.security;

public record AuthenticatedUser(
        String userId,
        String role,
        boolean emailVerified
) {
}
