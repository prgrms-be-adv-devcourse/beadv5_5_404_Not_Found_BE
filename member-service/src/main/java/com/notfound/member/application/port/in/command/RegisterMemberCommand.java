package com.notfound.member.application.port.in.command;

public record RegisterMemberCommand(
        String email,
        String password,
        String name,
        String phone
) {
    @Override
    public String toString() {
        return "RegisterMemberCommand[email=" + email
                + ", password=<redacted>, name=" + name
                + ", phone=" + phone + "]";
    }
}
