package com.notfound.payment.application.port.in;

import java.util.UUID;

public interface DeductDepositUseCase {

    DeductResult deduct(DeductCommand command);

    record DeductCommand(
            UUID memberId,
            UUID orderId,
            int amount,
            String description
    ) {}

    record DeductResult(
            UUID depositId,
            int balanceAfter
    ) {}
}
