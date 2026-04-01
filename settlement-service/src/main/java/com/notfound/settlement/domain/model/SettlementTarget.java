package com.notfound.settlement.domain.model;

import com.notfound.settlement.domain.exception.InvalidSettlementTargetStatusException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class SettlementTarget {

    private final UUID id;
    private final UUID orderId;
    private final UUID sellerId;
    private final long totalAmount;
    private final LocalDateTime confirmedAt;
    private UUID settlementId;
    private SettlementTargetStatus status;

    private SettlementTarget(UUID id, UUID orderId, UUID sellerId, long totalAmount,
                             LocalDateTime confirmedAt, UUID settlementId,
                             SettlementTargetStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.sellerId = sellerId;
        this.totalAmount = totalAmount;
        this.confirmedAt = confirmedAt;
        this.settlementId = settlementId;
        this.status = status;
    }

    public static SettlementTarget create(UUID orderId, UUID sellerId, long totalAmount,
                                          LocalDateTime confirmedAt) {
        return new SettlementTarget(UUID.randomUUID(), orderId, sellerId, totalAmount,
                confirmedAt, null, SettlementTargetStatus.PENDING);
    }

    public static SettlementTarget of(UUID id, UUID orderId, UUID sellerId, long totalAmount,
                                      LocalDateTime confirmedAt, UUID settlementId,
                                      SettlementTargetStatus status) {
        return new SettlementTarget(id, orderId, sellerId, totalAmount, confirmedAt,
                settlementId, status);
    }

    public void settle(UUID settlementId) {
        if (this.status != SettlementTargetStatus.PENDING) {
            throw new InvalidSettlementTargetStatusException(this.id, this.status);
        }
        this.settlementId = settlementId;
        this.status = SettlementTargetStatus.SETTLED;
    }
}
