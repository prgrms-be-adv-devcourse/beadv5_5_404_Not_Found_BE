package com.notfound.member.infrastructure.security;

import com.notfound.member.domain.model.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtProvider(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = properties.accessExpiration();
        this.refreshExpiration = properties.refreshExpiration();
    }

    public String createAccessToken(UUID memberId, MemberRole role, boolean emailVerified) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessExpiration);

        return Jwts.builder()
                .subject(memberId.toString())
                .id(UUID.randomUUID().toString())
                .claim("role", role.name())
                .claim("email_verified", emailVerified)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken() {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    public record BlacklistClaims(String jti, java.time.LocalDateTime expiresAt) {}

    /**
     * 토큰을 1회 파싱하여 jti와 만료시각(UTC)을 함께 반환한다.
     * 유효하지 않은 토큰이면 null을 반환한다.
     */
    public BlacklistClaims parseForBlacklist(String token) {
        try {
            Claims claims = parseClaims(token);
            String jti = claims.getId();
            java.time.LocalDateTime expiresAt = claims.getExpiration().toInstant()
                    .atZone(java.time.ZoneOffset.UTC)
                    .toLocalDateTime();
            return new BlacklistClaims(jti, expiresAt);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
