package com.notfound.order.application.port.in;

import com.notfound.order.domain.model.Order;

import java.util.List;
import java.util.UUID;

public interface CancelOrderUseCase {
    CancelOrderResult cancelOrder(UUID memberId, UUID orderId, List<UUID> orderItemIds);

    record CancelOrderResult(Order order, int refundAmount, List<UUID> cancelledItemIds) {}
}
