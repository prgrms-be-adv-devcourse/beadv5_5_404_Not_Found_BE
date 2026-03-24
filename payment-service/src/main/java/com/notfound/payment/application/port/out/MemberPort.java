package com.notfound.payment.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface MemberPort {

    boolean existsActiveMember(UUID memberId);

    int getDepositBalance(UUID memberId);

    SellerAccountInfo getSellerAccount(UUID sellerId);

    record SellerAccountInfo(
            UUID sellerId,
            String bankCode,
            String bankAccount,
            String accountHolder,
            BigDecimal commissionRate
    ) {}
}
