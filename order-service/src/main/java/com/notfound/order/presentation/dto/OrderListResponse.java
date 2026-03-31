package com.notfound.order.presentation.dto;

import com.notfound.order.domain.model.Order;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderListResponse(
        UUID orderId,
        String orderNumber,
        String orderStatus,
        int totalAmount,
        LocalDateTime createdAt
) {
    public static OrderListResponse from(Order order) {
        return new OrderListResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt());
    }
}
