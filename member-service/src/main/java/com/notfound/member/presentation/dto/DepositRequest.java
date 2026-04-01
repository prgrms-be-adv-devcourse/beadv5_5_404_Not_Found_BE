package com.notfound.member.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DepositRequest(
        @NotNull(message = "transactionId는 필수입니다.")
        String transactionId,

        @Min(value = 1, message = "금액은 1 이상이어야 합니다.")
        int amount
) {}
