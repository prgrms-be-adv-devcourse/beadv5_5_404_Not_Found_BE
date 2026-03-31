package com.notfound.order.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "상태는 필수입니다.")
        String status,
        int depositUsed
) {
}
