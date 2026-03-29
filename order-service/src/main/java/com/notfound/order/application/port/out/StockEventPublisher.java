package com.notfound.order.application.port.out;

import java.util.UUID;

public interface StockEventPublisher {
    void publishStockDeducted(UUID orderId, UUID productId, int quantity);
    void publishStockRestored(UUID orderId, UUID productId, int quantity);
}
