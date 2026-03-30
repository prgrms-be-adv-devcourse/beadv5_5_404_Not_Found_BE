package com.notfound.order.application.port.in;

import com.notfound.order.application.port.in.command.CreateOrderCommand;
import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderItem;
import java.util.List;
import java.util.UUID;

public interface CreateOrderUseCase {
    CreateOrderResult createOrder(UUID memberId, CreateOrderCommand command);

    record CreateOrderResult(Order order, List<OrderItem> orderItems) {}
}
