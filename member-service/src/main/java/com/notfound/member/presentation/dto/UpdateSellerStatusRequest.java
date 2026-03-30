package com.notfound.member.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSellerStatusRequest(
        @NotBlank(message = "상태를 입력해주세요.")
        String status,
        String reason
) {
}
