package com.notfound.product.adapter.out.rest;

import com.notfound.product.adapter.out.rest.dto.MemberApiResponse;
import com.notfound.product.adapter.out.rest.dto.SellerApprovedData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "member-service", configuration = MemberFeignClientConfig.class)
public interface MemberFeignClient {

    @GetMapping("/internal/seller/{memberId}/approved")
    MemberApiResponse<SellerApprovedData> isApprovedSeller(@PathVariable UUID memberId);
}
