package com.notfound.order.presentation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConfirmPurchaseResponse(
        UUID orderId,
        String orderStatus,
        LocalDateTime confirmedAt
) {
}
