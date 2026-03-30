package com.notfound.settlement.adapter.in.rest.dto;

import com.notfound.settlement.domain.model.Settlement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SettlementResponse(
        UUID id,
        UUID sellerId,
        LocalDate periodStart,
        LocalDate periodEnd,
        long totalSalesAmount,
        long feeAmount,
        long netAmount,
        LocalDateTime settledAt,
        String status
) {
    public static SettlementResponse from(Settlement settlement) {
        return new SettlementResponse(
                settlement.getId(),
                settlement.getSellerId(),
                settlement.getPeriodStart(),
                settlement.getPeriodEnd(),
                settlement.getTotalSalesAmount(),
                settlement.getFeeAmount(),
                settlement.getNetAmount(),
                settlement.getSettledAt(),
                settlement.getStatus().name()
        );
    }
}
