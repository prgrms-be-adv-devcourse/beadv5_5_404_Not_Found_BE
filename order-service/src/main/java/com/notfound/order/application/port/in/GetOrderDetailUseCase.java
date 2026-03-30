package com.notfound.order.application.port.in;

import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderItem;

import java.util.List;
import java.util.UUID;

public interface GetOrderDetailUseCase {
    OrderDetail getOrderDetail(UUID memberId, UUID orderId);

    record OrderDetail(Order order, List<OrderItem> orderItems) {}
}
