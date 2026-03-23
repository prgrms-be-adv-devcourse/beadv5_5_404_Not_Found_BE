package com.notfound.member.application.service;

import com.notfound.member.application.port.in.CheckSellerStatusUseCase;
import com.notfound.member.application.port.out.SellerRepository;
import com.notfound.member.domain.model.SellerStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SellerService implements CheckSellerStatusUseCase {

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
}
