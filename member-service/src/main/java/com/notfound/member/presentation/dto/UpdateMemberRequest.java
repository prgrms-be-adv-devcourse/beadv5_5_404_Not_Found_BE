package com.notfound.member.presentation.dto;

import com.notfound.member.application.port.in.command.UpdateMemberCommand;
import jakarta.validation.constraints.Size;

public record UpdateMemberRequest(
        @Size(min = 2, max = 20, message = "이름은 2~20자 사이여야 합니다.")
        String name,

        @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
        String phone,

        @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이여야 합니다.")
        String password,

        String currentPassword
) {

    public UpdateMemberCommand toCommand() {
        return new UpdateMemberCommand(name, phone, password, currentPassword);
    }
}
