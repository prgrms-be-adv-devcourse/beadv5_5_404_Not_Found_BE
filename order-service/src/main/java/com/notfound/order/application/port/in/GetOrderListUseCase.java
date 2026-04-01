package com.notfound.order.application.port.in;

import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetOrderListUseCase {
    Page<Order> getOrders(UUID memberId, OrderStatus status, Pageable pageable);
}
