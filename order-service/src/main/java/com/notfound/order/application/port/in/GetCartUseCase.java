package com.notfound.order.application.port.in;

import com.notfound.order.domain.model.Cart;
import com.notfound.order.domain.model.CartItem;
import java.util.List;
import java.util.UUID;

public interface GetCartUseCase {
    Cart getCart(UUID memberId);
    List<CartItem> getCartItems(UUID memberId);
}
