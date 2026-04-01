package com.notfound.payment.application.port.out;

import com.notfound.payment.domain.event.DepositChargedEvent;
import com.notfound.payment.domain.event.DepositDeductedEvent;
import com.notfound.payment.domain.event.DepositRefundedEvent;

public interface DepositEventPublisher {

    void publishDepositCharged(DepositChargedEvent event);

    void publishDepositDeducted(DepositDeductedEvent event);

    void publishDepositRefunded(DepositRefundedEvent event);
}
