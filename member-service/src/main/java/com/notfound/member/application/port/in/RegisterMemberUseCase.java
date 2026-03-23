package com.notfound.member.application.port.in;

import com.notfound.member.application.port.in.command.RegisterMemberCommand;
import com.notfound.member.application.port.in.result.AuthResult;

public interface RegisterMemberUseCase {

    AuthResult register(RegisterMemberCommand command, String userAgent, String ipAddress);
}
