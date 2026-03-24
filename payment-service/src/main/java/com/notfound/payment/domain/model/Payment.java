package com.notfound.payment.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Payment {

    private final UUID id;
    private final UUID orderId;
    private final PgProvider pgProvider;
    private final int amount;
    private PaymentStatus status;
    private String pgTransactionId;
    private String paymentKey;
    private final PaymentMethodType method;
    private final PaymentPurpose purpose;
    private LocalDateTime paidAt;
    private final String idempotencyKey;

    private Payment(
            UUID id,
            UUID orderId,
            PgProvider pgProvider,
            int amount,
            PaymentStatus status,
            String pgTransactionId,
            String paymentKey,
            PaymentMethodType method,
            PaymentPurpose purpose,
            LocalDateTime paidAt,
            String idempotencyKey
    ) {
        this.id = id;
        this.orderId = orderId;
        this.pgProvider = pgProvider;
        this.amount = amount;
        this.status = status;
        this.pgTransactionId = pgTransactionId;
        this.paymentKey = paymentKey;
        this.method = method;
        this.purpose = purpose;
        this.paidAt = paidAt;
        this.idempotencyKey = idempotencyKey;
    }

    public static Payment create(
            UUID orderId,
            PgProvider pgProvider,
            int amount,
            PaymentMethodType method,
            PaymentPurpose purpose,
            String idempotencyKey
    ) {
        return new Payment(
                UUID.randomUUID(),
                orderId,
                pgProvider,
                amount,
                PaymentStatus.PENDING,
                null,
                null,
                method,
                purpose,
                null,
                idempotencyKey
        );
    }

    public static Payment of(
            UUID id,
            UUID orderId,
            PgProvider pgProvider,
            int amount,
            PaymentStatus status,
            String pgTransactionId,
            String paymentKey,
            PaymentMethodType method,
            PaymentPurpose purpose,
            LocalDateTime paidAt,
            String idempotencyKey
    ) {
        return new Payment(id, orderId, pgProvider, amount, status,
                pgTransactionId, paymentKey, method, purpose, paidAt, idempotencyKey);
    }

    public void complete(String pgTransactionId, String paymentKey, LocalDateTime paidAt) {
        this.status = PaymentStatus.COMPLETED;
        this.pgTransactionId = pgTransactionId;
        this.paymentKey = paymentKey;
        this.paidAt = paidAt;
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public PgProvider getPgProvider() { return pgProvider; }
    public int getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getPgTransactionId() { return pgTransactionId; }
    public String getPaymentKey() { return paymentKey; }
    public PaymentMethodType getMethod() { return method; }
    public PaymentPurpose getPurpose() { return purpose; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getIdempotencyKey() { return idempotencyKey; }
}
