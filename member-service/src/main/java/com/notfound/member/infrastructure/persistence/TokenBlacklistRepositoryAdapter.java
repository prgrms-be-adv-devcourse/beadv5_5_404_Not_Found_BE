package com.notfound.member.infrastructure.persistence;

import com.notfound.member.application.port.out.TokenBlacklistRepository;
import com.notfound.member.domain.model.TokenBlacklist;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class TokenBlacklistRepositoryAdapter implements TokenBlacklistRepository {

    private static final String CACHE_NAME = "tokenBlacklist";

    private final TokenBlacklistJpaRepository tokenBlacklistJpaRepository;
    private final CacheManager cacheManager;

    public TokenBlacklistRepositoryAdapter(TokenBlacklistJpaRepository tokenBlacklistJpaRepository,
                                            CacheManager cacheManager) {
        this.tokenBlacklistJpaRepository = tokenBlacklistJpaRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    public void saveIfAbsent(TokenBlacklist tokenBlacklist) {
        tokenBlacklistJpaRepository.insertIfAbsent(
                tokenBlacklist.getJti(),
                tokenBlacklist.getExpiresAt(),
                LocalDateTime.now());

        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(tokenBlacklist.getJti(), true);
        }
    }

    @Override
    public boolean existsByJti(String jti) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            Boolean cached = cache.get(jti, Boolean.class);
            if (Boolean.TRUE.equals(cached)) {
                return true;
            }
        }

        return tokenBlacklistJpaRepository.existsByJti(jti);
    }

    @Override
    public int deleteExpiredTokens() {
        return tokenBlacklistJpaRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
