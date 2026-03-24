package com.notfound.payment.infrastructure.persistence;

import com.notfound.payment.domain.model.Payment;
import com.notfound.payment.domain.model.PaymentMethodType;
import com.notfound.payment.domain.model.PaymentPurpose;
import com.notfound.payment.domain.model.PaymentStatus;
import com.notfound.payment.domain.model.PgProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentJpaEntity {

    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "order_id", columnDefinition = "uuid")
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pg_provider", nullable = false, length = 20)
    private PgProvider pgProvider;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "pg_transaction_id", unique = true, length = 200)
    private String pgTransactionId;

    @Column(name = "payment_key", length = 500)
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethodType method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentPurpose purpose;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "idempotency_key", unique = true, nullable = false, length = 100)
    private String idempotencyKey;

    public static PaymentJpaEntity fromModel(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.id = payment.getId();
        entity.orderId = payment.getOrderId();
        entity.pgProvider = payment.getPgProvider();
        entity.amount = payment.getAmount();
        entity.status = payment.getStatus();
        entity.pgTransactionId = payment.getPgTransactionId();
        entity.paymentKey = payment.getPaymentKey();
        entity.method = payment.getMethod();
        entity.purpose = payment.getPurpose();
        entity.paidAt = payment.getPaidAt();
        entity.idempotencyKey = payment.getIdempotencyKey();
        return entity;
    }

    public Payment toModel() {
        return Payment.of(
                id,
                orderId,
                pgProvider,
                amount,
                status,
                pgTransactionId,
                paymentKey,
                method,
                purpose,
                paidAt,
                idempotencyKey
        );
    }
}
