package com.notfound.member.infrastructure.persistence;

import com.notfound.member.domain.model.TokenBlacklist;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist", indexes = {
        @Index(name = "idx_token_blacklist_expires_at", columnList = "expires_at")
})
public class TokenBlacklistJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static TokenBlacklistJpaEntity from(TokenBlacklist blacklist) {
        TokenBlacklistJpaEntity entity = new TokenBlacklistJpaEntity();
        entity.id = blacklist.getId();
        entity.jti = blacklist.getJti();
        entity.expiresAt = blacklist.getExpiresAt();
        entity.createdAt = blacklist.getCreatedAt();
        return entity;
    }

    public TokenBlacklist toDomain() {
        return TokenBlacklist.builder()
                .id(this.id)
                .jti(this.jti)
                .expiresAt(this.expiresAt)
                .createdAt(this.createdAt)
                .build();
    }
}
