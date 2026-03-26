package com.notfound.member.application.service;

import com.notfound.member.application.port.out.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TokenRevokeService {

    private final RefreshTokenRepository refreshTokenRepository;

    public TokenRevokeService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllByMemberId(UUID memberId) {
        refreshTokenRepository.revokeAllByMemberId(memberId);
    }
}
