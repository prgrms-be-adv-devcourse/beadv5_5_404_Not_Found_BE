package com.notfound.order.application.port.out;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PurchaseEventPublisher {
    void publishPurchaseConfirmed(UUID eventId, UUID orderId, UUID sellerId,
                                   long totalAmount, LocalDateTime confirmedAt);
}
