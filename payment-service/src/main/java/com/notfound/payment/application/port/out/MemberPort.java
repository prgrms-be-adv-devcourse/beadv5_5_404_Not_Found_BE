package com.notfound.payment.application.port.out;

import java.util.UUID;

public interface MemberPort {

    boolean existsActiveMember(UUID memberId);

    int getDepositBalance(UUID memberId);

    void updateDepositBalance(UUID memberId, int newBalance);
}
