package com.notfound.order.presentation.dto;

import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderItem;

import java.util.List;
import java.util.UUID;

public record InternalOrderResponse(
        UUID orderId,
        String status,
        int totalAmount,
        List<Item> items
) {
    public record Item(UUID productId, int quantity) {}

    public static InternalOrderResponse from(Order order, List<OrderItem> orderItems) {
        var items = orderItems.stream()
                .map(i -> new Item(i.getProductId(), i.getQuantity()))
                .toList();
        return new InternalOrderResponse(order.getId(), order.getStatus().name(), order.getTotalAmount(), items);
    }
}
