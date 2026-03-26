package com.notfound.product.adapter.in.kafka.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentApprovedEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        Payload payload
) {
    public record Payload(
            UUID orderId,
            UUID memberId,
            List<OrderItem> orderItems
    ) {}

    public record OrderItem(
            UUID productId,
            int quantity
    ) {}
}
