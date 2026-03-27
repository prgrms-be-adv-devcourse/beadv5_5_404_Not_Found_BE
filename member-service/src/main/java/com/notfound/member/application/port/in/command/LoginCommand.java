package com.notfound.member.application.port.in.command;

public record LoginCommand(
        String email,
        String password
) {
    @Override
    public String toString() {
        return "LoginCommand[email=" + email + ", password=<redacted>]";
    }
}
