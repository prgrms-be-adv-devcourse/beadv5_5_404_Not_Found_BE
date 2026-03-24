package com.notfound.payment.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Settlement {

    private final UUID id;
    private final UUID sellerId;
    private final UUID orderItemId;
    private final UUID paymentId;
    private final int grossAmount;
    private final int commissionAmount;
    private final int netAmount;
    private SettlementStatus status;
    private final LocalDate settlementDate;
    private LocalDateTime settledAt;

    private Settlement(
            UUID id,
            UUID sellerId,
            UUID orderItemId,
            UUID paymentId,
            int grossAmount,
            int commissionAmount,
            int netAmount,
            SettlementStatus status,
            LocalDate settlementDate,
            LocalDateTime settledAt
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.orderItemId = orderItemId;
        this.paymentId = paymentId;
        this.grossAmount = grossAmount;
        this.commissionAmount = commissionAmount;
        this.netAmount = netAmount;
        this.status = status;
        this.settlementDate = settlementDate;
        this.settledAt = settledAt;
    }

    public static Settlement create(
            UUID sellerId,
            UUID orderItemId,
            UUID paymentId,
            int grossAmount,
            int commissionAmount,
            int netAmount,
            LocalDate settlementDate
    ) {
        return new Settlement(
                UUID.randomUUID(),
                sellerId,
                orderItemId,
                paymentId,
                grossAmount,
                commissionAmount,
                netAmount,
                SettlementStatus.PENDING,
                settlementDate,
                null
        );
    }

    public static Settlement of(
            UUID id,
            UUID sellerId,
            UUID orderItemId,
            UUID paymentId,
            int grossAmount,
            int commissionAmount,
            int netAmount,
            SettlementStatus status,
            LocalDate settlementDate,
            LocalDateTime settledAt
    ) {
        return new Settlement(id, sellerId, orderItemId, paymentId, grossAmount,
                commissionAmount, netAmount, status, settlementDate, settledAt);
    }

    public void complete(LocalDateTime settledAt) {
        this.status = SettlementStatus.COMPLETED;
        this.settledAt = settledAt;
    }

    public UUID getId() { return id; }
    public UUID getSellerId() { return sellerId; }
    public UUID getOrderItemId() { return orderItemId; }
    public UUID getPaymentId() { return paymentId; }
    public int getGrossAmount() { return grossAmount; }
    public int getCommissionAmount() { return commissionAmount; }
    public int getNetAmount() { return netAmount; }
    public SettlementStatus getStatus() { return status; }
    public LocalDate getSettlementDate() { return settlementDate; }
    public LocalDateTime getSettledAt() { return settledAt; }
}
