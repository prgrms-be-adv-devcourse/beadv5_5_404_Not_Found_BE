package com.notfound.order.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateOrderStatusRequest(
        @NotNull(message = "상태는 필수입니다.")
        String status,

        @Min(value = 0, message = "예치금 사용 금액은 0 이상이어야 합니다.")
        int depositUsed,

        LocalDateTime confirmedAt
) {
}
