package com.notfound.member.application.service;

import com.notfound.member.application.port.in.ApproveSellerUseCase;
import com.notfound.member.application.port.in.CheckSellerRegisteredUseCase;
import com.notfound.member.application.port.in.CheckSellerStatusUseCase;
import com.notfound.member.application.port.in.GetSellerAccountUseCase;
import com.notfound.member.application.port.in.RegisterSellerUseCase;
import com.notfound.member.application.port.in.command.RegisterSellerCommand;
import com.notfound.member.application.port.out.MemberRepository;
import com.notfound.member.application.port.out.SellerRepository;
import com.notfound.member.domain.event.SellerApprovedEvent;
import com.notfound.member.domain.exception.MemberException;
import com.notfound.member.domain.model.MemberStatus;
import com.notfound.member.domain.model.Seller;
import com.notfound.member.domain.model.SellerStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class SellerService implements CheckSellerStatusUseCase, CheckSellerRegisteredUseCase,
        GetSellerAccountUseCase, RegisterSellerUseCase, ApproveSellerUseCase {

    private final SellerRepository sellerRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SellerService(SellerRepository sellerRepository,
                         MemberRepository memberRepository,
                         ApplicationEventPublisher eventPublisher) {
        this.sellerRepository = sellerRepository;
        this.memberRepository = memberRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSellerRegistered(UUID memberId) {
        return sellerRepository.existsByMemberId(memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isApprovedSeller(UUID memberId) {
        return sellerRepository.findByMemberId(memberId)
                .map(seller -> seller.getStatus() == SellerStatus.APPROVED)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Seller getSellerAccount(UUID memberId) {
        return sellerRepository.findByMemberId(memberId)
                .orElseThrow(MemberException::sellerNotFound);
    }

    @Override
    @Transactional
    public Seller registerSeller(UUID memberId, RegisterSellerCommand command) {
        memberRepository.findById(memberId)
                .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
                .orElseThrow(MemberException::inactiveAccount);

        if (sellerRepository.existsByMemberId(memberId)) {
            throw MemberException.sellerAlreadyRegistered();
        }

        Seller seller = Seller.builder()
                .memberId(memberId)
                .businessNumber(command.businessNumber())
                .shopName(command.shopName())
                .bankCode(command.bankCode())
                .bankAccount(command.bankAccount())
                .accountHolder(command.accountHolder())
                .commissionRate(BigDecimal.ZERO)
                .status(SellerStatus.PENDING)
                .build();

        return sellerRepository.save(seller);
    }

    @Override
    @Transactional
    public Seller updateSellerStatus(UUID memberId, SellerStatus status) {
        Seller seller = sellerRepository.findByMemberId(memberId)
                .orElseThrow(MemberException::sellerApplicationNotFound);

        switch (status) {
            case APPROVED -> {
                if (seller.getStatus() != SellerStatus.PENDING) {
                    throw MemberException.sellerNotPending();
                }
                seller.approve();
                Seller saved = sellerRepository.save(seller);
                eventPublisher.publishEvent(new SellerApprovedEvent(
                        saved.getMemberId(), saved.getId(), saved.getShopName()));
                return saved;
            }
            case SUSPENDED -> {
                if (seller.getStatus() == SellerStatus.SUSPENDED) {
                    throw new IllegalArgumentException("이미 정지된 판매자입니다.");
                }
                seller.suspend();
            }
            case PENDING -> throw new IllegalArgumentException("PENDING 상태로 변경할 수 없습니다.");
        }

        return sellerRepository.save(seller);
    }
}
