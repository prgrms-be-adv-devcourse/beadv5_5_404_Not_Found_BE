package com.notfound.settlement.adapter.out.rest;

import com.notfound.settlement.adapter.out.rest.dto.MemberApiResponse;
import com.notfound.settlement.adapter.out.rest.dto.SellerAccountData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "member-service", configuration = MemberFeignClientConfig.class)
public interface MemberFeignClient {

    @GetMapping("/member/seller/{sellerId}")
    MemberApiResponse<SellerAccountData> getSellerAccount(@PathVariable UUID sellerId);
}
