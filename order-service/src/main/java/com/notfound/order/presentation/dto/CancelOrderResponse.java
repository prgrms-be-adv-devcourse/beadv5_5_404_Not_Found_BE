package com.notfound.order.presentation.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CancelOrderResponse(
        UUID orderId,
        String orderStatus,
        int refundAmount,
        List<UUID> cancelledItemIds,
        LocalDateTime cancelledAt
) {
}
