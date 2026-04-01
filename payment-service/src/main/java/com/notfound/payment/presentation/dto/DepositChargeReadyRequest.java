package com.notfound.payment.presentation.dto;

import com.notfound.payment.application.port.in.PrepareDepositChargeUseCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

public record DepositChargeReadyRequest(
        @Min(1000) @Max(500000) int amount
) {
    public PrepareDepositChargeUseCase.PrepareCommand toCommand(UUID memberId) {
        return new PrepareDepositChargeUseCase.PrepareCommand(memberId, amount);
    }
}
