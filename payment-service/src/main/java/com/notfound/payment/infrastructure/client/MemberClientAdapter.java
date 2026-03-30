package com.notfound.payment.infrastructure.client;

import com.notfound.payment.application.port.out.MemberPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MemberClientAdapter implements MemberPort {

    private final MemberFeignClient memberFeignClient;

    @Override
    public boolean existsActiveMember(UUID memberId) {
        return memberFeignClient.getMemberActive(memberId).data().active();
    }

    @Override
    public int getDepositBalance(UUID memberId) {
        return memberFeignClient.getDepositBalance(memberId).data().depositBalance();
    }

    @Override
    public void deductDeposit(UUID memberId, int amount) {
        memberFeignClient.deductDeposit(memberId, new MemberFeignClient.AmountRequest(amount));
    }

    @Override
    public void chargeDeposit(UUID memberId, int amount) {
        memberFeignClient.chargeDeposit(memberId, new MemberFeignClient.AmountRequest(amount));
    }
}
