package com.notfound.payment.infrastructure.event;

import com.notfound.payment.application.port.out.MemberPort;
import com.notfound.payment.domain.event.DepositChargedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositChargedEventHandler {

    private final MemberPort memberPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DepositChargedEvent event) {
        memberPort.chargeDeposit(event.memberId(), event.chargedAmount());
        log.debug("예치금 충전 완료 — memberId={}, balanceAfter={}", event.memberId(), event.balanceAfter());
    }
}
