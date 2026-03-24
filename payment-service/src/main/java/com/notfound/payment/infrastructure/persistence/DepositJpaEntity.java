package com.notfound.payment.infrastructure.persistence;

import com.notfound.payment.domain.model.Deposit;
import com.notfound.payment.domain.model.DepositType;
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
@Table(name = "deposit")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepositJpaEntity {

    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "member_id", columnDefinition = "uuid", nullable = false)
    private UUID memberId;

    @Column(name = "payment_id", columnDefinition = "uuid")
    private UUID paymentId;

    @Column(name = "order_id", columnDefinition = "uuid")
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DepositType type;

    @Column(nullable = false)
    private int amount;

    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static DepositJpaEntity fromModel(Deposit deposit) {
        DepositJpaEntity entity = new DepositJpaEntity();
        entity.id = deposit.getId();
        entity.memberId = deposit.getMemberId();
        entity.paymentId = deposit.getPaymentId();
        entity.orderId = deposit.getOrderId();
        entity.type = deposit.getType();
        entity.amount = deposit.getAmount();
        entity.balanceAfter = deposit.getBalanceAfter();
        entity.description = deposit.getDescription();
        entity.createdAt = deposit.getCreatedAt();
        return entity;
    }

    public Deposit toModel() {
        return Deposit.of(
                id,
                memberId,
                paymentId,
                orderId,
                type,
                amount,
                balanceAfter,
                description,
                createdAt
        );
    }
}
