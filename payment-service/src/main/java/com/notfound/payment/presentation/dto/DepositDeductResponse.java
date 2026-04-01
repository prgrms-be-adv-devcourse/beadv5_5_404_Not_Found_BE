package com.notfound.payment.presentation.dto;

import com.notfound.payment.application.port.in.DeductDepositUseCase;

import java.util.UUID;

public record DepositDeductResponse(
        UUID depositId,
        int balanceAfter
) {
    public static DepositDeductResponse from(DeductDepositUseCase.DeductResult result) {
        return new DepositDeductResponse(result.depositId(), result.balanceAfter());
    }
}
