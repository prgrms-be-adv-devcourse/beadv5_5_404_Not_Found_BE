package com.notfound.member.infrastructure.security;

import com.notfound.member.domain.model.MemberRole;

import java.util.UUID;

public record JwtAuthentication(
        UUID memberId,
        MemberRole role,
        boolean emailVerified
) {
}
