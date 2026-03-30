package com.notfound.order.application.port.in;

import com.notfound.order.domain.model.Order;

import java.util.UUID;

public interface ConfirmPurchaseUseCase {
    Order confirmPurchase(UUID memberId, UUID orderId);
}
