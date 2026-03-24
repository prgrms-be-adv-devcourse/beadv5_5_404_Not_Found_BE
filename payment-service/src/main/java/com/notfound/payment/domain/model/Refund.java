package com.notfound.payment.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Refund {

    private final UUID id;
    private final UUID paymentId;
    private final UUID orderItemId;
    private final int amount;
    private final String reason;
    private RefundStatus status;
    private String pgRefundId;
    private LocalDateTime refundedAt;

    private Refund(
            UUID id,
            UUID paymentId,
            UUID orderItemId,
            int amount,
            String reason,
            RefundStatus status,
            String pgRefundId,
            LocalDateTime refundedAt
    ) {
        this.id = id;
        this.paymentId = paymentId;
        this.orderItemId = orderItemId;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.pgRefundId = pgRefundId;
        this.refundedAt = refundedAt;
    }

    public static Refund create(UUID paymentId, UUID orderItemId, int amount, String reason) {
        return new Refund(
                UUID.randomUUID(),
                paymentId,
                orderItemId,
                amount,
                reason,
                RefundStatus.PENDING,
                null,
                null
        );
    }

    public static Refund of(
            UUID id,
            UUID paymentId,
            UUID orderItemId,
            int amount,
            String reason,
            RefundStatus status,
            String pgRefundId,
            LocalDateTime refundedAt
    ) {
        return new Refund(id, paymentId, orderItemId, amount, reason, status, pgRefundId, refundedAt);
    }

    public void complete(String pgRefundId, LocalDateTime refundedAt) {
        this.status = RefundStatus.COMPLETED;
        this.pgRefundId = pgRefundId;
        this.refundedAt = refundedAt;
    }

    public void fail() {
        this.status = RefundStatus.FAILED;
    }

    public UUID getId() { return id; }
    public UUID getPaymentId() { return paymentId; }
    public UUID getOrderItemId() { return orderItemId; }
    public int getAmount() { return amount; }
    public String getReason() { return reason; }
    public RefundStatus getStatus() { return status; }
    public String getPgRefundId() { return pgRefundId; }
    public LocalDateTime getRefundedAt() { return refundedAt; }
}
