package com.notfound.payment.presentation.dto;

import com.notfound.payment.application.port.in.PrepareDepositChargeUseCase;

import java.util.UUID;

public record DepositChargeReadyResponse(
        UUID paymentId,
        int amount,
        String pgProvider,
        PgData pgData
) {
    public record PgData(
            String clientKey,
            String orderId,
            int amount,
            String orderName,
            String successUrl,
            String failUrl
    ) {}

    public static DepositChargeReadyResponse from(PrepareDepositChargeUseCase.PrepareResult result) {
        PrepareDepositChargeUseCase.PrepareResult.PgData src = result.pgData();
        return new DepositChargeReadyResponse(
                result.paymentId(),
                result.amount(),
                result.pgProvider(),
                new PgData(src.clientKey(), src.orderId(), src.amount(), src.orderName(), src.successUrl(), src.failUrl())
        );
    }
}
