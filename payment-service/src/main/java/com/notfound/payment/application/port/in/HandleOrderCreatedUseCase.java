package com.notfound.payment.application.port.in;

import java.util.UUID;

public interface HandleOrderCreatedUseCase {

    void handle(OrderCreatedCommand command);

    record OrderCreatedCommand(
            UUID eventId,
            UUID orderId,
            UUID memberId,
            int totalAmount,
            int depositUsed,
            int paymentAmount,
            String idempotencyKey
    ) {}
}
