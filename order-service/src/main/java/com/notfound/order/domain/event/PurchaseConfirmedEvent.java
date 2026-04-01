package com.notfound.order.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseConfirmedEvent(
        UUID eventId,
        UUID orderId,
        UUID sellerId,
        long totalAmount,
        LocalDateTime confirmedAt
) {}
