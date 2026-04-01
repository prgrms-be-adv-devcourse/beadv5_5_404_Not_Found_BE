package com.notfound.order.application.port.out;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PurchaseEventPublisher {
    void publishPurchaseConfirmed(UUID orderId, UUID sellerId, int totalAmount, LocalDateTime confirmedAt);
}
