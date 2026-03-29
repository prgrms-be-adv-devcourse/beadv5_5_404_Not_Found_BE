package com.notfound.payment.presentation.dto;

import com.notfound.payment.application.port.in.ConfirmDepositChargeUseCase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record DepositChargeConfirmRequest(
        @NotBlank String paymentKey,
        @NotBlank String orderId,
        @Positive int amount
) {
    public ConfirmDepositChargeUseCase.ConfirmCommand toCommand(UUID memberId) {
        return new ConfirmDepositChargeUseCase.ConfirmCommand(memberId, paymentKey, orderId, amount);
    }
}
