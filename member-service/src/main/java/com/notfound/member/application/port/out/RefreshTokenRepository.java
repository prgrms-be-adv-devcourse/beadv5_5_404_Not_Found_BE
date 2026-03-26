package com.notfound.member.application.port.out;

import com.notfound.member.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    boolean revokeByTokenHash(String tokenHash);

    void revokeAllByMemberId(UUID memberId);

    int deleteExpiredTokens();
}
