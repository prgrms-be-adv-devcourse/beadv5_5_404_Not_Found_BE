package com.notfound.payment.application.port.in;

import java.util.UUID;

public interface PayOrderUseCase {

    PayResult pay(PayCommand command);

    record PayCommand(
            UUID memberId,
            UUID orderId
    ) {}

    record PayResult(
            UUID orderId,
            int depositUsed,
            int balanceAfter
    ) {}
}
