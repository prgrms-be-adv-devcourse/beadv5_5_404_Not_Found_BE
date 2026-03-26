package com.notfound.member.application.service;

import com.notfound.member.application.port.in.CheckSellerStatusUseCase;
import com.notfound.member.application.port.in.GetSellerAccountUseCase;
import com.notfound.member.application.port.out.SellerRepository;
import com.notfound.member.domain.exception.MemberException;
import com.notfound.member.domain.model.Seller;
import com.notfound.member.domain.model.SellerStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SellerService implements CheckSellerStatusUseCase, GetSellerAccountUseCase {

    private final SellerRepository sellerRepository;

    public SellerService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
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
}
