package com.notfound.member.application.service;

import com.notfound.member.application.port.in.ChargeDepositUseCase;
import com.notfound.member.application.port.in.CheckMemberActiveUseCase;
import com.notfound.member.application.port.in.DeductDepositUseCase;
import com.notfound.member.application.port.in.GetDepositBalanceUseCase;
import com.notfound.member.application.port.in.GetMemberProfileUseCase;
import com.notfound.member.application.port.in.UpdateMemberUseCase;
import com.notfound.member.application.port.in.WithdrawMemberUseCase;
import com.notfound.member.application.port.in.command.UpdateMemberCommand;
import com.notfound.member.application.port.out.MemberRepository;
import com.notfound.member.application.port.out.TokenBlacklistRepository;
import com.notfound.member.infrastructure.persistence.ProcessedDepositTransactionEntity;
import com.notfound.member.infrastructure.persistence.ProcessedDepositTransactionEntity.TransactionType;
import com.notfound.member.infrastructure.persistence.ProcessedDepositTransactionRepository;
import com.notfound.member.domain.exception.MemberException;
import com.notfound.member.domain.model.Member;
import com.notfound.member.domain.model.MemberStatus;
import com.notfound.member.domain.model.TokenBlacklist;
import com.notfound.member.infrastructure.security.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MemberService implements CheckMemberActiveUseCase, GetDepositBalanceUseCase,
        DeductDepositUseCase, ChargeDepositUseCase,
        GetMemberProfileUseCase, UpdateMemberUseCase, WithdrawMemberUseCase {

    private final MemberRepository memberRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final ProcessedDepositTransactionRepository transactionRepository;
    private final TokenRevokeService tokenRevokeService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository,
                         TokenBlacklistRepository tokenBlacklistRepository,
                         ProcessedDepositTransactionRepository transactionRepository,
                         TokenRevokeService tokenRevokeService,
                         JwtProvider jwtProvider,
                         PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.transactionRepository = transactionRepository;
        this.tokenRevokeService = tokenRevokeService;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActiveMember(UUID memberId) {
        return memberRepository.findById(memberId)
                .map(member -> member.getStatus() == MemberStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public int getDepositBalance(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);
        return member.getDepositBalance();
    }

    @Override
    @Transactional
    public int deductDeposit(UUID memberId, int amount, String transactionId) {
        var existing = transactionRepository.findById(transactionId);
        if (existing.isPresent()) {
            return existing.get().getRemainingBalance();
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);
        member.deductDeposit(amount);
        memberRepository.save(member);

        transactionRepository.save(new ProcessedDepositTransactionEntity(
                transactionId, memberId, TransactionType.DEDUCT, amount, member.getDepositBalance()));

        return member.getDepositBalance();
    }

    @Override
    @Transactional
    public int chargeDeposit(UUID memberId, int amount, String transactionId) {
        var existing = transactionRepository.findById(transactionId);
        if (existing.isPresent()) {
            return existing.get().getRemainingBalance();
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);
        member.chargeDeposit(amount);
        memberRepository.save(member);

        transactionRepository.save(new ProcessedDepositTransactionEntity(
                transactionId, memberId, TransactionType.CHARGE, amount, member.getDepositBalance()));

        return member.getDepositBalance();
    }

    @Override
    @Transactional(readOnly = true)
    public Member getProfile(UUID memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);
    }

    @Override
    @Transactional
    public Member updateMember(UUID memberId, UpdateMemberCommand command) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);

        if (command.password() != null && !command.password().isBlank()) {
            if (command.currentPassword() == null || command.currentPassword().isBlank()) {
                throw MemberException.invalidPassword();
            }
            if (!passwordEncoder.matches(command.currentPassword(), member.getPasswordHash())) {
                throw MemberException.invalidPassword();
            }
            member.changePassword(passwordEncoder.encode(command.password()));
            // 비밀번호 변경 시 기존 세션 전부 무효화
            tokenRevokeService.revokeAllByMemberId(memberId);
        }

        member.updateProfile(command.name(), command.phone());
        return memberRepository.save(member);
    }

    @Override
    @Transactional
    public void withdraw(UUID memberId, String password, String accessToken) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);

        if (!passwordEncoder.matches(password, member.getPasswordHash())) {
            throw MemberException.invalidPassword();
        }

        member.withdraw();
        memberRepository.save(member);

        tokenRevokeService.revokeAllByMemberId(memberId);

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
    }
}
