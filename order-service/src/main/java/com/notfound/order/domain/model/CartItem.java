package com.notfound.order.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class CartItem {

    private UUID id;
    private UUID cartId;
    private UUID productId;
    private int quantity;
    private LocalDateTime createdAt;

    private CartItem() {}

    public static Builder builder() { return new Builder(); }

    public UUID getId() { return id; }
    public UUID getCartId() { return cartId; }
    public UUID getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    public static class Builder {
        private final CartItem item = new CartItem();
        public Builder id(UUID id) { item.id = id; return this; }
        public Builder cartId(UUID cartId) { item.cartId = cartId; return this; }
        public Builder productId(UUID productId) { item.productId = productId; return this; }
        public Builder quantity(int quantity) { item.quantity = quantity; return this; }
        public Builder createdAt(LocalDateTime createdAt) { item.createdAt = createdAt; return this; }
        public CartItem build() { return item; }
    }
}
