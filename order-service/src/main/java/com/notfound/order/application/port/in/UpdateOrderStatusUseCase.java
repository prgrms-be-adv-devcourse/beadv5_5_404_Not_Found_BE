package com.notfound.order.application.port.in;

import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderStatus;

import java.util.UUID;

public interface UpdateOrderStatusUseCase {
    Order updateStatus(UUID orderId, OrderStatus status, int depositUsed);
}
