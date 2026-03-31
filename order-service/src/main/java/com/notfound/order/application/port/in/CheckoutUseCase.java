package com.notfound.order.application.port.in;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CheckoutUseCase {
    Map<String, Object> checkout(UUID memberId, List<UUID> cartItemIds, UUID productId, Integer quantity);
}
