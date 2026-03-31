package com.notfound.order.presentation.dto;

import java.util.List;
import java.util.UUID;

public record ReturnResponse(
        UUID orderId,
        String returnStatus,
        List<UUID> orderItemIds
) {
}
