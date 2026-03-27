package com.notfound.member.presentation.controller;

import com.notfound.member.application.port.out.TokenBlacklistRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/token-blacklist")
public class InternalTokenBlacklistController {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public InternalTokenBlacklistController(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @GetMapping("/{jti}")
    public ResponseEntity<Boolean> isBlacklisted(@PathVariable String jti) {
        return ResponseEntity.ok(tokenBlacklistRepository.existsByJti(jti));
    }
}
