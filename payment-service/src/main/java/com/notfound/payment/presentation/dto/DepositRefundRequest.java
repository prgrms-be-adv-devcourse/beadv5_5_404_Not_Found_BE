package com.notfound.payment.presentation.dto;

import com.notfound.payment.application.port.in.RefundDepositUseCase;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record DepositRefundRequest(
        @NotNull UUID memberId,
        @NotNull UUID orderId,
        @Positive int amount,
        String description
) {
    public RefundDepositUseCase.RefundCommand toCommand() {
        return new RefundDepositUseCase.RefundCommand(memberId, orderId, amount, description);
    }
}
