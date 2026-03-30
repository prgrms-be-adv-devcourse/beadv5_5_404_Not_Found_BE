package com.notfound.member.application.port.in;

import java.util.UUID;

public interface WithdrawMemberUseCase {
    void withdraw(UUID memberId, String password, String accessToken);
}
