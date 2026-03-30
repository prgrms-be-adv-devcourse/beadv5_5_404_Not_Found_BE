package com.notfound.order.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Order {

    private UUID id;
    private String orderNumber;
    private UUID memberId;
    private OrderStatus status;
    private int totalAmount;
    private int shippingFee;
    private int depositUsed;
    private String shippingSnapshot;
    private String idempotencyKey;
    private LocalDateTime createdAt;

    private Order() {}

    public static Builder builder() { return new Builder(); }

    public UUID getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public UUID getMemberId() { return memberId; }
    public OrderStatus getStatus() { return status; }
    public int getTotalAmount() { return totalAmount; }
    public int getShippingFee() { return shippingFee; }
    public int getDepositUsed() { return depositUsed; }
    public String getShippingSnapshot() { return shippingSnapshot; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    public void confirm() {
        this.status = OrderStatus.PURCHASE_CONFIRMED;
    }

    public static class Builder {
        private final Order order = new Order();
        public Builder id(UUID id) { order.id = id; return this; }
        public Builder orderNumber(String orderNumber) { order.orderNumber = orderNumber; return this; }
        public Builder memberId(UUID memberId) { order.memberId = memberId; return this; }
        public Builder status(OrderStatus status) { order.status = status; return this; }
        public Builder totalAmount(int totalAmount) { order.totalAmount = totalAmount; return this; }
        public Builder shippingFee(int shippingFee) { order.shippingFee = shippingFee; return this; }
        public Builder depositUsed(int depositUsed) { order.depositUsed = depositUsed; return this; }
        public Builder shippingSnapshot(String shippingSnapshot) { order.shippingSnapshot = shippingSnapshot; return this; }
        public Builder idempotencyKey(String idempotencyKey) { order.idempotencyKey = idempotencyKey; return this; }
        public Builder createdAt(LocalDateTime createdAt) { order.createdAt = createdAt; return this; }
        public Order build() { return order; }
    }
}
