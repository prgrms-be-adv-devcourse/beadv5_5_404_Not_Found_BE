package com.notfound.payment.presentation.dto;

import com.notfound.payment.application.port.in.PrepareDepositChargeUseCase;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record DepositChargeReadyRequest(
        @Positive int amount
) {
    public PrepareDepositChargeUseCase.PrepareCommand toCommand(UUID memberId) {
        return new PrepareDepositChargeUseCase.PrepareCommand(memberId, amount);
    }
}
