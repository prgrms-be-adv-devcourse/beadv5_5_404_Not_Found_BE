package com.notfound.member.presentation.dto;

import com.notfound.member.application.port.in.result.AuthResult;

import java.util.UUID;

public record AuthResponse(
        UUID memberId,
        String accessToken
) {

    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.memberId(), result.accessToken());
    }
}
