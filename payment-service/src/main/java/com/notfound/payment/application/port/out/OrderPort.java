package com.notfound.payment.application.port.out;

import java.util.UUID;

public interface OrderPort {

    OrderInfo getOrder(UUID orderId);

    record OrderInfo(
            UUID orderId,
            UUID memberId,
            String orderStatus,
            int totalAmount,
            int depositUsed,
            int paymentAmount
    ) {}
}
