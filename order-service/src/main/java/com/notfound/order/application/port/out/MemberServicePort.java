package com.notfound.order.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MemberServicePort {
    boolean isActiveMember(UUID memberId);
    List<Map<String, Object>> getAddresses(UUID memberId);
    int getDepositBalance(UUID memberId);
    int deductDeposit(UUID memberId, int amount);
    int chargeDeposit(UUID memberId, int amount);
}
