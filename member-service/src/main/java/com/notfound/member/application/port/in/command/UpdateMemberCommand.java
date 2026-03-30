package com.notfound.member.application.port.in.command;

public record UpdateMemberCommand(
        String name,
        String phone,
        String password,
        String currentPassword
) {
}
