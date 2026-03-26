package com.notfound.product.adapter.out.rest;

import com.notfound.product.application.port.out.SellerStatusVerifier;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SellerStatusVerifierAdapter implements SellerStatusVerifier {

    private final MemberFeignClient memberFeignClient;

    public SellerStatusVerifierAdapter(MemberFeignClient memberFeignClient) {
        this.memberFeignClient = memberFeignClient;
    }

    @Override
    public boolean isApprovedSeller(UUID memberId) {
        return memberFeignClient.isApprovedSeller(memberId).data().approved();
    }
}
