package com.notfound.member.application.port.in.command;

public record LoginCommand(
        String email,
        String password
) {
}
