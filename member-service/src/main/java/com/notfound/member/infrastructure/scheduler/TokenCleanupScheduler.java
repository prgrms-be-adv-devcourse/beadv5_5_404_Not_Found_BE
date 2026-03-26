package com.notfound.member.infrastructure.scheduler;

import com.notfound.member.application.port.in.CleanupExpiredTokensUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenCleanupScheduler {

    private final CleanupExpiredTokensUseCase cleanupExpiredTokensUseCase;

    public TokenCleanupScheduler(CleanupExpiredTokensUseCase cleanupExpiredTokensUseCase) {
        this.cleanupExpiredTokensUseCase = cleanupExpiredTokensUseCase;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredTokens() {
        cleanupExpiredTokensUseCase.cleanupExpiredTokens();
    }
}
