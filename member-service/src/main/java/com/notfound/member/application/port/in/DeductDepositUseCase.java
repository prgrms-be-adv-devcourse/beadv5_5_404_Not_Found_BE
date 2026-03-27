package com.notfound.member.application.port.in;

import java.util.UUID;

public interface DeductDepositUseCase {

    int deductDeposit(UUID memberId, int amount);
}
