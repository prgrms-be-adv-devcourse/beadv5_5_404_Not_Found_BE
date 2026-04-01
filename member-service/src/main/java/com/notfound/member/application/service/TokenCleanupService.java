package com.notfound.member.application.service;

import com.notfound.member.application.port.in.CleanupExpiredTokensUseCase;
import com.notfound.member.application.port.out.RefreshTokenRepository;
import com.notfound.member.application.port.out.TokenBlacklistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenCleanupService implements CleanupExpiredTokensUseCase {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public TokenCleanupService(RefreshTokenRepository refreshTokenRepository,
                                TokenBlacklistRepository tokenBlacklistRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        int deletedRefreshTokens = refreshTokenRepository.deleteExpiredTokens();
        int deletedBlacklistTokens = tokenBlacklistRepository.deleteExpiredTokens();

        log.info("토큰 정리 완료 - RefreshToken: {}건, Blacklist: {}건 삭제",
                deletedRefreshTokens, deletedBlacklistTokens);
    }
}
