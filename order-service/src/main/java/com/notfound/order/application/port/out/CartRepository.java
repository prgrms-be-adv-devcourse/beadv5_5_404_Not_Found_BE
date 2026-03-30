package com.notfound.order.application.port.out;

import com.notfound.order.domain.model.Cart;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository {
    Optional<Cart> findByMemberId(UUID memberId);
    Cart save(Cart cart);
}
