package com.notfound.order.application.port.in.command;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
        List<OrderItemCommand> items,
        UUID addressId
) {
    public record OrderItemCommand(
            UUID productId,
            int quantity,
            UUID cartItemId
    ) {}
}
