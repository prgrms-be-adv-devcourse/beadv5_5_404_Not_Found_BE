package com.notfound.payment.application.port.out;

import java.util.List;
import java.util.UUID;

public interface OrderPort {

    OrderDetail getOrder(UUID orderId);

    void updateOrderStatus(UUID orderId, String status, int depositUsed);

    record OrderDetail(
            UUID orderId,
            String status,
            int totalAmount,
            List<OrderItem> items
    ) {}

    record OrderItem(
            UUID productId,
            int quantity
    ) {}
}
