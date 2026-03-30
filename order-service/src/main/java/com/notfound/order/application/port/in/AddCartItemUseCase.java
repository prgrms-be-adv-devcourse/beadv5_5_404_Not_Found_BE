package com.notfound.order.application.port.in;

import com.notfound.order.domain.model.CartItem;
import java.util.UUID;

public interface AddCartItemUseCase {
    CartItem addCartItem(UUID memberId, UUID productId, int quantity);
}
