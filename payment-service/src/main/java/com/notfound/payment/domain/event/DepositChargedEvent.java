package com.notfound.payment.domain.event;

import java.util.UUID;

public record DepositChargedEvent(
        UUID memberId,
        int chargedAmount,
        int balanceAfter,
        String transactionId
) {}
