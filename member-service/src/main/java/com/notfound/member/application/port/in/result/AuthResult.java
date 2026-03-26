package com.notfound.member.application.port.in.result;

import java.util.UUID;

public record AuthResult(
        UUID memberId,
        String accessToken,
        String refreshToken
) {
    @Override
    public String toString() {
        return "AuthResult[memberId=" + memberId + "]";
    }
}
