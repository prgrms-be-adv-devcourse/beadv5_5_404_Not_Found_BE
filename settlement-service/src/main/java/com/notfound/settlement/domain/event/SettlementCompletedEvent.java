package com.notfound.settlement.domain.event;

import java.util.UUID;

public record SettlementCompletedEvent(
        UUID settlementId,
        UUID sellerId,
        long netAmount
) {}
