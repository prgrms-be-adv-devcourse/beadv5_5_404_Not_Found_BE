package com.notfound.member.application.service;

import com.notfound.member.application.port.in.LoginUseCase;
import com.notfound.member.application.port.in.LogoutUseCase;
import com.notfound.member.application.port.in.RefreshTokenUseCase;
import com.notfound.member.application.port.in.RegisterMemberUseCase;
import com.notfound.member.application.port.in.command.LoginCommand;
import com.notfound.member.application.port.in.command.RegisterMemberCommand;
import com.notfound.member.application.port.in.result.AuthResult;
import com.notfound.member.application.port.out.MemberRepository;
import com.notfound.member.application.port.out.RefreshTokenRepository;
import com.notfound.member.application.port.out.TokenBlacklistRepository;
import com.notfound.member.domain.exception.MemberException;
import com.notfound.member.domain.model.Member;
import com.notfound.member.domain.model.MemberRole;
import com.notfound.member.domain.model.MemberStatus;
import com.notfound.member.domain.model.RefreshToken;
import com.notfound.member.domain.model.TokenBlacklist;
import com.notfound.member.infrastructure.security.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService implements RegisterMemberUseCase, LoginUseCase, RefreshTokenUseCase, LogoutUseCase {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final TokenRevokeService tokenRevokeService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MemberRepository memberRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       TokenBlacklistRepository tokenBlacklistRepository,
                       TokenRevokeService tokenRevokeService,
                       JwtProvider jwtProvider,
                       PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.tokenRevokeService = tokenRevokeService;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AuthResult register(RegisterMemberCommand command, String userAgent, String ipAddress) {
        if (memberRepository.existsByEmail(command.email())) {
            throw MemberException.duplicateEmail();
        }

        Member member = Member.builder()
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .name(command.name())
                .phone(command.phone())
                .role(MemberRole.USER)
                .status(MemberStatus.ACTIVE)
                .pointBalance(0)
                .depositBalance(0)
                .emailVerified(false)
                .build();

        Member savedMember = memberRepository.save(member);

        return issueTokens(savedMember, userAgent, ipAddress);
    }

    @Override
    @Transactional
    public AuthResult login(LoginCommand command, String userAgent, String ipAddress) {
        Member member = memberRepository.findByEmail(command.email())
                .orElseThrow(MemberException::invalidCredentials);

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw MemberException.inactiveAccount();
        }

        if (!passwordEncoder.matches(command.password(), member.getPasswordHash())) {
            throw MemberException.invalidCredentials();
        }

        return issueTokens(member, userAgent, ipAddress);
    }

    @Override
    @Transactional
    public AuthResult refresh(String rawRefreshToken, String userAgent, String ipAddress) {
        if (!jwtProvider.validateToken(rawRefreshToken)) {
            throw MemberException.invalidRefreshToken();
        }

        String tokenHash = hashToken(rawRefreshToken);

        // 원자적 1건 revoke: affected rows == 1이면 성공, 0이면 이미 폐기됨
        boolean revoked = refreshTokenRepository.revokeByTokenHash(tokenHash);

        if (!revoked) {
            // 이미 폐기된 토큰 → 탈취 감지 (Token Rotation 위반)
            // DB에 해당 해시가 존재하는지 확인하여 탈취 vs 존재하지 않는 토큰 구분
            RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                    .orElseThrow(MemberException::invalidRefreshToken);

            // 존재하지만 revoked → 탈취 감지 → 해당 회원 전체 토큰 무효화
            // 별도 트랜잭션(REQUIRES_NEW)으로 실행하여 예외 롤백과 무관하게 무효화 보장
            tokenRevokeService.revokeAllByMemberId(storedToken.getMemberId());
            throw MemberException.tokenHijacked();
        }

        // revoke 성공한 토큰의 memberId 조회
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(MemberException::invalidRefreshToken);

        Member member = memberRepository.findById(storedToken.getMemberId())
                .orElseThrow(MemberException::notFound);

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw MemberException.inactiveAccount();
        }

        return issueTokens(member, userAgent, ipAddress);
    }

    @Override
    @Transactional
    public void logout(String accessToken, String rawRefreshToken) {
        // Access Token → 블랙리스트 등록 (1회 파싱, UTC 고정)
        if (accessToken != null) {
            JwtProvider.BlacklistClaims claims = jwtProvider.parseForBlacklist(accessToken);
            if (claims != null) {
                TokenBlacklist blacklist = TokenBlacklist.builder()
                        .jti(claims.jti())
                        .expiresAt(claims.expiresAt())
                        .build();
                tokenBlacklistRepository.saveIfAbsent(blacklist);
            }
        }

        // Refresh Token → 해당 토큰 1건만 폐기 (다른 기기 세션 유지)
        if (rawRefreshToken != null) {
            String tokenHash = hashToken(rawRefreshToken);
            refreshTokenRepository.revokeByTokenHash(tokenHash);
        }
    }

    private AuthResult issueTokens(Member member, String userAgent, String ipAddress) {
        String accessToken = jwtProvider.createAccessToken(
                member.getId(), member.getRole(), member.isEmailVerified());
        String rawRefreshToken = jwtProvider.createRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(member.getId())
                .tokenHash(hashToken(rawRefreshToken))
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProvider.getRefreshExpiration())))
                .lastUsedAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(refreshToken);

        return new AuthResult(member.getId(), accessToken, rawRefreshToken);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

}
