package com.notfound.member.infrastructure.persistence;

import com.notfound.member.application.port.out.RefreshTokenRepository;
import com.notfound.member.domain.model.RefreshToken;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository refreshTokenJpaRepository,
                                         MemberJpaRepository memberJpaRepository) {
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        MemberJpaEntity memberEntity = memberJpaRepository.getReferenceById(refreshToken.getMemberId());
        RefreshTokenJpaEntity entity = RefreshTokenJpaEntity.from(refreshToken, memberEntity);
        RefreshTokenJpaEntity saved = refreshTokenJpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenJpaRepository.findByTokenHash(tokenHash).map(RefreshTokenJpaEntity::toDomain);
    }

    @Override
    public boolean revokeByTokenHash(String tokenHash) {
        return refreshTokenJpaRepository.revokeByTokenHash(tokenHash) == 1;
    }

    @Override
    public void revokeAllByMemberId(UUID memberId) {
        refreshTokenJpaRepository.revokeAllByMemberId(memberId);
    }

    @Override
    public int deleteExpiredTokens() {
        return refreshTokenJpaRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
