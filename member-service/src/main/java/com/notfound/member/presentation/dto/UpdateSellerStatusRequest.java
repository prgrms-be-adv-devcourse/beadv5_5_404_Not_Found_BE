package com.notfound.member.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSellerStatusRequest(
        @NotBlank(message = "상태를 입력해주세요.")
        String status,

        @Size(max = 500, message = "사유는 500자 이내로 입력해주세요.")
        String reason
) {
}
