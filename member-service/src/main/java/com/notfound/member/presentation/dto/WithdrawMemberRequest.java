package com.notfound.member.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record WithdrawMemberRequest(
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {
}
