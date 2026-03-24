package com.notfound.payment.infrastructure.persistence;

import com.notfound.payment.domain.model.Refund;
import com.notfound.payment.domain.model.RefundStatus;
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
@Table(name = "refund")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundJpaEntity {

    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "payment_id", columnDefinition = "uuid", nullable = false)
    private UUID paymentId;

    @Column(name = "order_item_id", columnDefinition = "uuid", nullable = false)
    private UUID orderItemId;

    @Column(nullable = false)
    private int amount;

    @Column(length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status;

    @Column(name = "pg_refund_id", length = 200)
    private String pgRefundId;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    public static RefundJpaEntity fromModel(Refund refund) {
        RefundJpaEntity entity = new RefundJpaEntity();
        entity.id = refund.getId();
        entity.paymentId = refund.getPaymentId();
        entity.orderItemId = refund.getOrderItemId();
        entity.amount = refund.getAmount();
        entity.reason = refund.getReason();
        entity.status = refund.getStatus();
        entity.pgRefundId = refund.getPgRefundId();
        entity.refundedAt = refund.getRefundedAt();
        return entity;
    }

    public Refund toModel() {
        return Refund.of(
                id,
                paymentId,
                orderItemId,
                amount,
                reason,
                status,
                pgRefundId,
                refundedAt
        );
    }
}
