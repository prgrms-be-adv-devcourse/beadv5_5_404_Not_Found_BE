package com.notfound.payment.presentation.dto;

import com.notfound.payment.application.port.in.GetDepositHistoryUseCase;
import com.notfound.payment.domain.model.DepositType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DepositHistoryResponse(
        List<DepositEntryDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static DepositHistoryResponse from(GetDepositHistoryUseCase.HistoryResult result) {
        return new DepositHistoryResponse(
                result.content().stream().map(DepositEntryDto::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }

    public record DepositEntryDto(
            UUID depositId,
            DepositType type,
            int amount,
            int balanceAfter,
            String description,
            UUID orderId,
            LocalDateTime createdAt
    ) {
        public static DepositEntryDto from(GetDepositHistoryUseCase.DepositEntry entry) {
            return new DepositEntryDto(
                    entry.depositId(),
                    entry.type(),
                    entry.amount(),
                    entry.balanceAfter(),
                    entry.description(),
                    entry.orderId(),
                    entry.createdAt()
            );
        }
    }
}
