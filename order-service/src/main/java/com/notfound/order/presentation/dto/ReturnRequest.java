package com.notfound.order.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record ReturnRequest(
        @NotBlank(message = "반품 사유는 필수입니다.")
        String reason,

        @NotEmpty(message = "반품 대상 항목을 선택해주세요.")
        List<UUID> orderItemIds
) {
}
