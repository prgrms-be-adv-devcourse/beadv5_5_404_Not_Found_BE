package com.notfound.order.application.port.in;

import java.util.UUID;

public interface DeleteCartItemUseCase {
    void deleteCartItem(UUID memberId, UUID cartItemId);
}
