package com.notfound.member.application.port.in;

import com.notfound.member.application.port.in.result.AuthResult;

public interface RefreshTokenUseCase {

    AuthResult refresh(String refreshToken, String userAgent, String ipAddress);
}
