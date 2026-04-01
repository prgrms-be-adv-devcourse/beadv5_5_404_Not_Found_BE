package com.notfound.payment.application.port.out;

import java.util.UUID;

public interface MemberPort {

    boolean existsActiveMember(UUID memberId);

    int getDepositBalance(UUID memberId);

    void deductDeposit(UUID memberId, int amount, String transactionId);

    void chargeDeposit(UUID memberId, int amount, String transactionId);
}
