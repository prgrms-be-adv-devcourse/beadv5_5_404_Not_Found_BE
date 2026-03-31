package com.notfound.order.application.port.in;

import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderItem;

import java.util.List;
import java.util.UUID;

public interface GetInternalOrderUseCase {
    InternalOrderDetail getOrder(UUID orderId);

    record InternalOrderDetail(Order order, List<OrderItem> orderItems) {}
}
