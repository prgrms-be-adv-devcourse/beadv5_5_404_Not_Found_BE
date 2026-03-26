package com.notfound.member.application.port.out;

import com.notfound.member.domain.model.MemberRole;

import java.util.Date;
import java.util.UUID;

public interface TokenProvider {

    String createAccessToken(UUID memberId, MemberRole role, boolean emailVerified);

    String createRefreshToken();

    boolean validateToken(String token);

    String getJti(String token);

    Date getExpiration(String token);

    long getRefreshExpiration();
}
