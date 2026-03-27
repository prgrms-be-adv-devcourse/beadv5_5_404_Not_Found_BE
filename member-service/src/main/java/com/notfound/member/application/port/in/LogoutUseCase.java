package com.notfound.member.application.port.in;

public interface LogoutUseCase {

    void logout(String accessToken, String refreshToken);
}
