package com.notfound.order.presentation.dto;

import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String orderNumber,
        String orderStatus,
        List<OrderItemResponse> items,
        int totalAmount,
        int shippingFee,
        int depositUsed,
        LocalDateTime createdAt
) {
    public record OrderItemResponse(
            UUID orderItemId,
            UUID productId,
            String productName,
            int price,
            int quantity,
            int subtotal
    ) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                    item.getId(),
                    item.getProductId(),
                    item.getProductTitle(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    item.getSubtotal());
        }
    }

    public static OrderResponse from(Order order, List<OrderItem> items) {
        var itemResponses = items.stream()
                .map(OrderItemResponse::from)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                itemResponses,
                order.getTotalAmount(),
                order.getShippingFee(),
                order.getDepositUsed(),
                order.getCreatedAt());
    }
}
