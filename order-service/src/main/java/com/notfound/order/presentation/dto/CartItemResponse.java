package com.notfound.order.presentation.dto;

import com.notfound.order.domain.model.CartItem;
import java.util.UUID;

public record CartItemResponse(
        UUID cartItemId,
        UUID productId,
        int quantity
) {
    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(item.getId(), item.getProductId(), item.getQuantity());
    }
}
