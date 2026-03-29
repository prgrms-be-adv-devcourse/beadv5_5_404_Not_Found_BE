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
        return memberFeignClient.getMemberActive(memberId).active();
    }

    @Override
    public int getDepositBalance(UUID memberId) {
        return memberFeignClient.getDepositBalance(memberId).depositBalance();
    }

    @Override
    public void updateDepositBalance(UUID memberId, int newBalance) {
        memberFeignClient.updateDepositBalance(memberId,
                new MemberFeignClient.UpdateDepositBalanceRequest(newBalance));
    }
}
