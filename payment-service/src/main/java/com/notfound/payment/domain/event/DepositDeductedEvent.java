package com.notfound.payment.domain.event;

import java.util.UUID;

public record DepositDeductedEvent(
        UUID memberId,
        int amount,
        String transactionId
) {}
