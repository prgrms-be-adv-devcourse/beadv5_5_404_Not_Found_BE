package com.notfound.member.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class RefreshToken {

    private UUID id;
    private UUID memberId;
    private String tokenHash;
    private String userAgent;
    private String ipAddress;
    private boolean revoked;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;

    private RefreshToken() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public static class Builder {
        private final RefreshToken token = new RefreshToken();

        public Builder id(UUID id) {
            token.id = id;
            return this;
        }

        public Builder memberId(UUID memberId) {
            token.memberId = memberId;
            return this;
        }

        public Builder tokenHash(String tokenHash) {
            token.tokenHash = tokenHash;
            return this;
        }

        public Builder userAgent(String userAgent) {
            token.userAgent = userAgent;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            token.ipAddress = ipAddress;
            return this;
        }

        public Builder revoked(boolean revoked) {
            token.revoked = revoked;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            token.expiresAt = expiresAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            token.createdAt = createdAt;
            return this;
        }

        public Builder lastUsedAt(LocalDateTime lastUsedAt) {
            token.lastUsedAt = lastUsedAt;
            return this;
        }

        public RefreshToken build() {
            return token;
        }
    }
}
