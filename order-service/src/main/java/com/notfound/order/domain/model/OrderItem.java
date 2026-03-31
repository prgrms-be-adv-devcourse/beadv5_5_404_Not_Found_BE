package com.notfound.order.domain.model;

import java.util.UUID;

public class OrderItem {

    private UUID id;
    private UUID orderId;
    private UUID productId;
    private UUID sellerId;
    private String productTitle;
    private int unitPrice;
    private int quantity;
    private int subtotal;
    private OrderItemStatus status;

    private OrderItem() {}

    public static Builder builder() { return new Builder(); }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getProductId() { return productId; }
    public UUID getSellerId() { return sellerId; }
    public String getProductTitle() { return productTitle; }
    public int getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public int getSubtotal() { return subtotal; }
    public OrderItemStatus getStatus() { return status; }

    public void cancel() {
        this.status = OrderItemStatus.CANCELLED;
    }

    public static class Builder {
        private final OrderItem item = new OrderItem();
        public Builder id(UUID id) { item.id = id; return this; }
        public Builder orderId(UUID orderId) { item.orderId = orderId; return this; }
        public Builder productId(UUID productId) { item.productId = productId; return this; }
        public Builder sellerId(UUID sellerId) { item.sellerId = sellerId; return this; }
        public Builder productTitle(String productTitle) { item.productTitle = productTitle; return this; }
        public Builder unitPrice(int unitPrice) { item.unitPrice = unitPrice; return this; }
        public Builder quantity(int quantity) { item.quantity = quantity; return this; }
        public Builder subtotal(int subtotal) { item.subtotal = subtotal; return this; }
        public Builder status(OrderItemStatus status) { item.status = status; return this; }
        public OrderItem build() { return item; }
    }
}
