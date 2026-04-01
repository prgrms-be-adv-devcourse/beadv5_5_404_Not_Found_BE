package com.notfound.payment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "member-service", configuration = InternalFeignConfig.class)
public interface MemberFeignClient {

    @GetMapping("/internal/member/{memberId}/active")
    ApiResponse<MemberActiveData> getMemberActive(@PathVariable UUID memberId);

    @GetMapping("/internal/member/{memberId}/deposit")
    ApiResponse<DepositBalanceData> getDepositBalance(@PathVariable UUID memberId);

    @PostMapping("/internal/member/{memberId}/deposit/deduct")
    ApiResponse<Object> deductDeposit(@PathVariable UUID memberId, @RequestBody DepositRequest request);

    @PostMapping("/internal/member/{memberId}/deposit/charge")
    ApiResponse<Object> chargeDeposit(@PathVariable UUID memberId, @RequestBody DepositRequest request);

    record ApiResponse<T>(int status, String code, String message, T data) {}

    record MemberActiveData(boolean active) {}

    record DepositBalanceData(int depositBalance) {}

    record DepositRequest(String transactionId, int amount) {}
}
