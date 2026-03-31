package com.notfound.payment.application.port.out;

import com.notfound.payment.domain.event.DepositChargedEvent;

public interface DepositEventPublisher {

    void publishDepositCharged(DepositChargedEvent event);
}
