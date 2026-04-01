package com.notfound.member.infrastructure.persistence;

import com.notfound.member.domain.model.RefreshToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_token", indexes = {
        @Index(name = "idx_refresh_token_hash", columnList = "token_hash"),
        @Index(name = "idx_refresh_token_member_revoked", columnList = "member_id, is_revoked")
})
public class RefreshTokenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberJpaEntity member;

    @Column(name = "token_hash", nullable = false, unique = true, length = 512)
    private String tokenHash;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "is_revoked", nullable = false)
    private boolean revoked;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public static RefreshTokenJpaEntity from(RefreshToken token, MemberJpaEntity memberEntity) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.id = token.getId();
        entity.member = memberEntity;
        entity.tokenHash = token.getTokenHash();
        entity.userAgent = token.getUserAgent();
        entity.ipAddress = token.getIpAddress();
        entity.revoked = token.isRevoked();
        entity.expiresAt = token.getExpiresAt();
        entity.createdAt = token.getCreatedAt();
        entity.lastUsedAt = token.getLastUsedAt();
        return entity;
    }

    public RefreshToken toDomain() {
        return RefreshToken.builder()
                .id(this.id)
                .memberId(this.member.getId())
                .tokenHash(this.tokenHash)
                .userAgent(this.userAgent)
                .ipAddress(this.ipAddress)
                .revoked(this.revoked)
                .expiresAt(this.expiresAt)
                .createdAt(this.createdAt)
                .lastUsedAt(this.lastUsedAt)
                .build();
    }
}
