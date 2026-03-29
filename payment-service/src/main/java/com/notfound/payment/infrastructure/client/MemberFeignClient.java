package com.notfound.payment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "member-service")
public interface MemberFeignClient {

    @GetMapping("/internal/members/{memberId}/active")
    MemberActiveResponse getMemberActive(@PathVariable UUID memberId);

    @GetMapping("/internal/members/{memberId}/deposit-balance")
    DepositBalanceResponse getDepositBalance(@PathVariable UUID memberId);

    @PatchMapping("/internal/members/{memberId}/deposit-balance")
    void updateDepositBalance(@PathVariable UUID memberId, @RequestBody UpdateDepositBalanceRequest request);

    record MemberActiveResponse(boolean active) {}

    record DepositBalanceResponse(int depositBalance) {}

    record UpdateDepositBalanceRequest(int depositBalance) {}
}
