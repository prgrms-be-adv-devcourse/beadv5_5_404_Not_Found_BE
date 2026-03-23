package com.notfound.member.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TokenBlacklistJpaRepository extends JpaRepository<TokenBlacklistJpaEntity, Long> {

    boolean existsByJti(String jti);

    @Modifying
    @Query(value = "INSERT INTO token_blacklist (jti, expires_at, created_at) VALUES (:jti, :expiresAt, :createdAt) ON CONFLICT (jti) DO NOTHING",
            nativeQuery = true)
    void insertIfAbsent(@Param("jti") String jti,
                        @Param("expiresAt") LocalDateTime expiresAt,
                        @Param("createdAt") LocalDateTime createdAt);

    @Modifying
    @Query("DELETE FROM TokenBlacklistJpaEntity t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
}
