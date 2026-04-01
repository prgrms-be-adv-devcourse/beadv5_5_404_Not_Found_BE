package com.notfound.payment.presentation.dto;

import com.notfound.payment.application.port.in.PayOrderUseCase;

import java.util.UUID;

public record PayOrderResponse(
        UUID orderId,
        int depositUsed,
        int balanceAfter
) {
    public static PayOrderResponse from(PayOrderUseCase.PayResult result) {
        return new PayOrderResponse(result.orderId(), result.depositUsed(), result.balanceAfter());
    }
}
