package com.notfound.settlement.application.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateSettlementTargetCommand(
        String eventId,
        UUID orderId,
        UUID sellerId,
        long totalAmount,
        LocalDateTime confirmedAt
) {}
