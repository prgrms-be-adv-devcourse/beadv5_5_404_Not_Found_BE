package com.notfound.member.application.port.out;

import com.notfound.member.domain.model.TokenBlacklist;

public interface TokenBlacklistRepository {

    void saveIfAbsent(TokenBlacklist tokenBlacklist);

    boolean existsByJti(String jti);

    int deleteExpiredTokens();
}
