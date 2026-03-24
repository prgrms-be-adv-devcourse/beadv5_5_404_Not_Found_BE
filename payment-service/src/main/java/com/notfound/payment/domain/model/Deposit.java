package com.notfound.payment.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Deposit {

    private final UUID id;
    private final UUID memberId;
    private final UUID paymentId;
    private final UUID orderId;
    private final DepositType type;
    private final int amount;
    private final int balanceAfter;
    private final String description;
    private final LocalDateTime createdAt;

    private Deposit(
            UUID id,
            UUID memberId,
            UUID paymentId,
            UUID orderId,
            DepositType type,
            int amount,
            int balanceAfter,
            String description,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static Deposit create(
            UUID memberId,
            UUID paymentId,
            UUID orderId,
            DepositType type,
            int amount,
            int balanceAfter,
            String description
    ) {
        return new Deposit(
                UUID.randomUUID(),
                memberId,
                paymentId,
                orderId,
                type,
                amount,
                balanceAfter,
                description,
                LocalDateTime.now()
        );
    }

    public static Deposit of(
            UUID id,
            UUID memberId,
            UUID paymentId,
            UUID orderId,
            DepositType type,
            int amount,
            int balanceAfter,
            String description,
            LocalDateTime createdAt
    ) {
        return new Deposit(id, memberId, paymentId, orderId, type, amount, balanceAfter, description, createdAt);
    }

    public UUID getId() { return id; }
    public UUID getMemberId() { return memberId; }
    public UUID getPaymentId() { return paymentId; }
    public UUID getOrderId() { return orderId; }
    public DepositType getType() { return type; }
    public int getAmount() { return amount; }
    public int getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
