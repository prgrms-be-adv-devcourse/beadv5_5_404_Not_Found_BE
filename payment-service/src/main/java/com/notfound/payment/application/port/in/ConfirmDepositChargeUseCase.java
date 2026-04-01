package com.notfound.payment.application.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ConfirmDepositChargeUseCase {

    ConfirmResult confirm(ConfirmCommand command);

    record ConfirmCommand(
            UUID memberId,
            String paymentKey,
            String orderId,
            int amount
    ) {}

    record ConfirmResult(
            UUID paymentId,
            int chargedAmount,
            int balanceAfter,
            String pgTransactionId,
            String method,
            LocalDateTime paidAt
    ) {}
}
