package com.notfound.order.application.port.out;

import java.util.UUID;

public interface PurchaseEventPublisher {
    void publishPurchaseConfirmed(UUID orderId, UUID memberId, int totalAmount);
}
