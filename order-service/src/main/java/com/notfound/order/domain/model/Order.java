package com.notfound.order.domain.model;

import com.notfound.order.domain.exception.InvalidStateTransitionException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private UUID addressId;
    private String cartItemIds;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime confirmedAt;
    private Long version;

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
    public UUID getAddressId() { return addressId; }
    public String getCartItemIds() { return cartItemIds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public Long getVersion() { return version; }

    /**
     * cartItemIds를 UUID 리스트로 파싱.
     * 포맷: "uuid1,uuid2,..." (콤마 구분). null/빈값/잘못된 UUID는 안전하게 무시.
     */
    public List<UUID> parseCartItemIds() {
        if (cartItemIds == null || cartItemIds.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(cartItemIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try { return UUID.fromString(s); }
                    catch (IllegalArgumentException e) { return null; }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * UUID 리스트를 cartItemIds 문자열로 직렬화.
     */
    public static String serializeCartItemIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return ids.stream().map(UUID::toString).collect(Collectors.joining(","));
    }

    /**
     * PENDING → PAID 전이. 멱등 처리: 이미 PAID면 true 반환(부작용 없음).
     * @return true=이미 PAID(멱등), false=정상 전이
     */
    public boolean pay(int depositUsed, String shippingSnapshot) {
        if (this.status == OrderStatus.PAID) {
            return true; // 멱등: 이미 결제 완료
        }
        if (this.status != OrderStatus.PENDING) {
            throw new InvalidStateTransitionException("PENDING 상태에서만 결제 가능합니다: " + this.status);
        }
        this.status = OrderStatus.PAID;
        this.depositUsed = depositUsed;
        this.shippingSnapshot = shippingSnapshot;
        return false;
    }

    public void cancel() {
        if (this.status != OrderStatus.PENDING && this.status != OrderStatus.PAID && this.status != OrderStatus.CONFIRMED) {
            throw new InvalidStateTransitionException("취소 가능 상태가 아닙니다: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    public boolean confirmPurchase() {
        if (this.status == OrderStatus.PURCHASE_CONFIRMED) {
            return true;
        }
        if (this.status != OrderStatus.DELIVERED) {
            throw new InvalidStateTransitionException("구매확정 가능 상태가 아닙니다: " + this.status);
        }
        this.status = OrderStatus.PURCHASE_CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        return false;
    }

    public void markShipping() {
        if (this.status != OrderStatus.PAID && this.status != OrderStatus.CONFIRMED) {
            throw new InvalidStateTransitionException("배송 시작 가능 상태가 아닙니다: " + this.status);
        }
        this.status = OrderStatus.SHIPPING;
    }

    public void markDelivered() {
        if (this.status != OrderStatus.SHIPPING) {
            throw new InvalidStateTransitionException("배송 완료 처리 가능 상태가 아닙니다: " + this.status);
        }
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
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
        public Builder addressId(UUID addressId) { order.addressId = addressId; return this; }
        public Builder cartItemIds(String cartItemIds) { order.cartItemIds = cartItemIds; return this; }
        public Builder createdAt(LocalDateTime createdAt) { order.createdAt = createdAt; return this; }
        public Builder deliveredAt(LocalDateTime deliveredAt) { order.deliveredAt = deliveredAt; return this; }
        public Builder confirmedAt(LocalDateTime confirmedAt) { order.confirmedAt = confirmedAt; return this; }
        public Builder version(Long version) { order.version = version; return this; }
        public Order build() { return order; }
    }
}
