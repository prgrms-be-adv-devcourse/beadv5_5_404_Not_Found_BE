package com.notfound.order.presentation.dto;

import com.notfound.order.domain.model.Cart;
import com.notfound.order.domain.model.CartItem;

import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID cartId,
        List<CartItemResponse> items
) {
    public static CartResponse from(Cart cart, List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream()
                .map(CartItemResponse::from)
                .toList();
        return new CartResponse(cart.getId(), itemResponses);
    }
}
