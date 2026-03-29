package com.notfound.order.application.port.in;

import com.notfound.order.domain.model.CartItem;
import java.util.UUID;

public interface UpdateCartItemUseCase {
    CartItem updateCartItemQuantity(UUID memberId, UUID cartItemId, int quantity);
}
