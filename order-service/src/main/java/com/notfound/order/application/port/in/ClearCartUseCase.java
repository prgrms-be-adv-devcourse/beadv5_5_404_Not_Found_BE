package com.notfound.order.application.port.in;

import java.util.UUID;

public interface ClearCartUseCase {
    void clearCart(UUID memberId);
}
