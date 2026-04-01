package com.notfound.member.application.port.in;

import java.util.UUID;

public interface ChargeDepositUseCase {
    int chargeDeposit(UUID memberId, int amount, String transactionId);
}
