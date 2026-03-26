package com.notfound.member.application.port.in;

import com.notfound.member.application.port.in.command.LoginCommand;
import com.notfound.member.application.port.in.result.AuthResult;

public interface LoginUseCase {

    AuthResult login(LoginCommand command, String userAgent, String ipAddress);
}
