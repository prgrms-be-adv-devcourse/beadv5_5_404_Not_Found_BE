package com.notfound.payment.presentation.dto;

import com.notfound.payment.application.port.in.RefundDepositUseCase;

import java.util.UUID;

public record DepositRefundResponse(
        UUID depositId,
        int balanceAfter
) {
    public static DepositRefundResponse from(RefundDepositUseCase.RefundResult result) {
        return new DepositRefundResponse(result.depositId(), result.balanceAfter());
    }
}
