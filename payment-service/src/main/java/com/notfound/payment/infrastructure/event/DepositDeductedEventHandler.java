package com.notfound.payment.infrastructure.event;

import com.notfound.payment.application.port.out.MemberPort;
import com.notfound.payment.domain.event.DepositDeductedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositDeductedEventHandler {

    private final MemberPort memberPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DepositDeductedEvent event) {
        try {
            memberPort.deductDeposit(event.memberId(), event.amount(), event.transactionId());
        } catch (Exception e) {
            log.error("예치금 차감 동기화 실패 — memberId={}, amount={}, transactionId={}",
                    event.memberId(), event.amount(), event.transactionId(), e);
        }
    }
}
