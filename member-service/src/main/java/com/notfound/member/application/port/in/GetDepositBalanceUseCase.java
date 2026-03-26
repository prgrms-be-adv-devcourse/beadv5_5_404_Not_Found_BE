package com.notfound.member.application.port.in;

import java.util.UUID;

public interface GetDepositBalanceUseCase {

    int getDepositBalance(UUID memberId);
}
