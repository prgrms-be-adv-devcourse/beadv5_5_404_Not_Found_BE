package com.notfound.settlement.adapter.in.kafka.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record PurchaseConfirmedEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        Payload payload
) {
    public PurchaseConfirmedEvent {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId must not be blank");
        }
        Objects.requireNonNull(payload, "payload must not be null");
    }

    public record Payload(
            UUID orderId,
            UUID sellerId,
            List<OrderItem> orderItems,
            long totalAmount,
            LocalDateTime confirmedAt
    ) {}

    public record OrderItem(
            UUID productId,
            int quantity,
            int price
    ) {}
}
