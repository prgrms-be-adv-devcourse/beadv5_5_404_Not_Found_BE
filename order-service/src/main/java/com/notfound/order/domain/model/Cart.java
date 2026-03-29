package com.notfound.order.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Cart {

    private UUID id;
    private UUID memberId;
    private LocalDateTime createdAt;

    private Cart() {}

    public static Builder builder() { return new Builder(); }

    public UUID getId() { return id; }
    public UUID getMemberId() { return memberId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static class Builder {
        private final Cart cart = new Cart();
        public Builder id(UUID id) { cart.id = id; return this; }
        public Builder memberId(UUID memberId) { cart.memberId = memberId; return this; }
        public Builder createdAt(LocalDateTime createdAt) { cart.createdAt = createdAt; return this; }
        public Cart build() { return cart; }
    }
}
