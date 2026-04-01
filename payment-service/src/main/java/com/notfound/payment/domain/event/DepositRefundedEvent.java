package com.notfound.payment.domain.event;

import java.util.UUID;

public record DepositRefundedEvent(
        UUID memberId,
        int amount,
        String transactionId
) {}
