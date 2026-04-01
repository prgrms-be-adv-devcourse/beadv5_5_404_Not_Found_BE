package com.notfound.settlement.domain.model;

import com.notfound.settlement.domain.exception.InvalidSettlementStatusException;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class Settlement {

    private final UUID id;
    private final UUID sellerId;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final long totalSalesAmount;
    private final long feeAmount;
    private final long netAmount;
    private final LocalDateTime settledAt;
    private SettlementStatus status;

    private Settlement(UUID id, UUID sellerId, LocalDate periodStart, LocalDate periodEnd,
                       long totalSalesAmount, long feeAmount, long netAmount,
                       LocalDateTime settledAt, SettlementStatus status) {
        this.id = id;
        this.sellerId = sellerId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalSalesAmount = totalSalesAmount;
        this.feeAmount = feeAmount;
        this.netAmount = netAmount;
        this.settledAt = settledAt;
        this.status = status;
    }

    public static Settlement create(UUID sellerId, LocalDate periodStart, LocalDate periodEnd,
                                    List<SettlementTarget> targets, double feeRate) {
        long totalSalesAmount = targets.stream()
                .mapToLong(SettlementTarget::getTotalAmount)
                .sum();
        long feeAmount = Math.round(totalSalesAmount * feeRate);
        long netAmount = totalSalesAmount - feeAmount;

        return new Settlement(UUID.randomUUID(), sellerId, periodStart, periodEnd,
                totalSalesAmount, feeAmount, netAmount,
                LocalDateTime.now(), SettlementStatus.PENDING);
    }

    public static Settlement of(UUID id, UUID sellerId, LocalDate periodStart, LocalDate periodEnd,
                                long totalSalesAmount, long feeAmount, long netAmount,
                                LocalDateTime settledAt, SettlementStatus status) {
        return new Settlement(id, sellerId, periodStart, periodEnd, totalSalesAmount,
                feeAmount, netAmount, settledAt, status);
    }

    public void complete() {
        if (this.status != SettlementStatus.PENDING) {
            throw new InvalidSettlementStatusException(this.id, this.status);
        }
        this.status = SettlementStatus.COMPLETED;
    }

    public void fail() {
        if (this.status != SettlementStatus.PENDING) {
            throw new InvalidSettlementStatusException(this.id, this.status);
        }
        this.status = SettlementStatus.FAILED;
    }

    public void reset() {
        if (this.status != SettlementStatus.FAILED) {
            throw new InvalidSettlementStatusException(this.id, this.status);
        }
        this.status = SettlementStatus.PENDING;
    }
}
