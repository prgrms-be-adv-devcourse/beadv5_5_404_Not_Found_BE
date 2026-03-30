package com.notfound.member.presentation.dto;

import com.notfound.member.application.port.in.command.UpdateMemberCommand;

public record UpdateMemberRequest(
        String name,
        String phone,
        String password,
        String currentPassword
) {
    public UpdateMemberCommand toCommand() {
        return new UpdateMemberCommand(name, phone, password, currentPassword);
    }
}
