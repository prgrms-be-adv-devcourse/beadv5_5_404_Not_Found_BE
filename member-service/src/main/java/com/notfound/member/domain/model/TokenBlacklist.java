package com.notfound.member.domain.model;

import java.time.LocalDateTime;

public class TokenBlacklist {

    private Long id;
    private String jti;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    private TokenBlacklist() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public String getJti() {
        return jti;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static class Builder {
        private final TokenBlacklist blacklist = new TokenBlacklist();

        public Builder id(Long id) {
            blacklist.id = id;
            return this;
        }

        public Builder jti(String jti) {
            blacklist.jti = jti;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            blacklist.expiresAt = expiresAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            blacklist.createdAt = createdAt;
            return this;
        }

        public TokenBlacklist build() {
            return blacklist;
        }
    }
}
