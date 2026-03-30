package com.notfound.settlement.domain.event;

import java.time.LocalDate;
import java.util.UUID;

public record SettlementFailedEvent(
        UUID sellerId,
        LocalDate periodStart,
        LocalDate periodEnd,
        String reason
) {}
