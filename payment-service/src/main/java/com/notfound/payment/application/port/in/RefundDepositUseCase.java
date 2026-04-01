package com.notfound.payment.application.port.in;

import java.util.UUID;

public interface RefundDepositUseCase {

    RefundResult refund(RefundCommand command);

    record RefundCommand(
            UUID memberId,
            UUID orderId,
            int amount,
            String description
    ) {}

    record RefundResult(
            UUID depositId,
            int balanceAfter
    ) {}
}
