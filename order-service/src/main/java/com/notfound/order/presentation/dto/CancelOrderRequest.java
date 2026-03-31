package com.notfound.order.presentation.dto;

import java.util.List;
import java.util.UUID;

public record CancelOrderRequest(
        List<UUID> orderItemIds
) {
}
