package com.notfound.order.application.port.out;

import com.notfound.order.domain.model.OrderItem;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository {
    OrderItem save(OrderItem orderItem);
    List<OrderItem> saveAll(List<OrderItem> orderItems);
    List<OrderItem> findByOrderId(UUID orderId);
}
