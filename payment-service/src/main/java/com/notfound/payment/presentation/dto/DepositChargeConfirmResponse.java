package com.notfound.payment.presentation.dto;

import com.notfound.payment.application.port.in.ConfirmDepositChargeUseCase;

import java.time.LocalDateTime;
import java.util.UUID;

public record DepositChargeConfirmResponse(
        UUID paymentId,
        int chargedAmount,
        int balanceAfter,
        String pgTransactionId,
        String method,
        LocalDateTime paidAt
) {
    public static DepositChargeConfirmResponse from(ConfirmDepositChargeUseCase.ConfirmResult result) {
        return new DepositChargeConfirmResponse(
                result.paymentId(),
                result.chargedAmount(),
                result.balanceAfter(),
                result.pgTransactionId(),
                result.method(),
                result.paidAt()
        );
    }
}
