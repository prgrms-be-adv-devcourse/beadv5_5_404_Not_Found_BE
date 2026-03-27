package com.notfound.product.adapter.out.rest;

import com.notfound.product.adapter.out.rest.dto.MemberApiResponse;
import com.notfound.product.adapter.out.rest.dto.SellerApprovedData;
import com.notfound.product.application.port.out.SellerStatusVerifier;
import com.notfound.product.domain.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SellerStatusVerifierAdapter implements SellerStatusVerifier {

    private final MemberFeignClient memberFeignClient;

    @Override
    public boolean isApprovedSeller(UUID memberId) {
        MemberApiResponse<SellerApprovedData> response = memberFeignClient.isApprovedSeller(memberId);
        if (response.data() == null) {
            throw new ForbiddenException("판매자 정보를 확인할 수 없습니다.");
        }
        return response.data().approved();
    }
}
