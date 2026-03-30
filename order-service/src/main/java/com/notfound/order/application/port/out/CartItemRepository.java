package com.notfound.order.application.port.out;

import com.notfound.order.domain.model.CartItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository {
    List<CartItem> findByCartId(UUID cartId);
    Optional<CartItem> findById(UUID cartItemId);
    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);
    CartItem save(CartItem cartItem);
    void deleteById(UUID cartItemId);
    void deleteByCartId(UUID cartId);
}
