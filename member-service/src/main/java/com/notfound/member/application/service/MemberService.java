package com.notfound.member.application.service;

import com.notfound.member.application.port.in.ChargeDepositUseCase;
import com.notfound.member.application.port.in.CheckMemberActiveUseCase;
import com.notfound.member.application.port.in.DeductDepositUseCase;
import com.notfound.member.application.port.in.GetDepositBalanceUseCase;
import com.notfound.member.application.port.out.MemberRepository;
import com.notfound.member.domain.exception.MemberException;
import com.notfound.member.domain.model.Member;
import com.notfound.member.domain.model.MemberStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MemberService implements CheckMemberActiveUseCase, GetDepositBalanceUseCase,
        DeductDepositUseCase, ChargeDepositUseCase {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
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
    public int deductDeposit(UUID memberId, int amount) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);
        member.deductDeposit(amount);
        memberRepository.save(member);
        return member.getDepositBalance();
    }

    @Override
    @Transactional
    public int chargeDeposit(UUID memberId, int amount) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);
        member.chargeDeposit(amount);
        memberRepository.save(member);
        return member.getDepositBalance();
    }
}
