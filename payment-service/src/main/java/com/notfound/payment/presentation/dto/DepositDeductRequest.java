package com.notfound.payment.presentation.dto;

import com.notfound.payment.application.port.in.DeductDepositUseCase;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record DepositDeductRequest(
        UUID memberId,
        UUID orderId,
        @Positive int amount,
        String description
) {
    public DeductDepositUseCase.DeductCommand toCommand() {
        return new DeductDepositUseCase.DeductCommand(memberId, orderId, amount, description);
    }
}
