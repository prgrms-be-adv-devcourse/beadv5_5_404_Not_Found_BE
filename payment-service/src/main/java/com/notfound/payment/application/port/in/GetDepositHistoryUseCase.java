package com.notfound.payment.application.port.in;

import com.notfound.payment.domain.model.DepositType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GetDepositHistoryUseCase {

    HistoryResult getHistory(UUID memberId, DepositType type, int page, int size);

    record HistoryResult(
            List<DepositEntry> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    record DepositEntry(
            UUID depositId,
            DepositType type,
            int amount,
            int balanceAfter,
            String description,
            UUID orderId,
            LocalDateTime createdAt
    ) {}
}
