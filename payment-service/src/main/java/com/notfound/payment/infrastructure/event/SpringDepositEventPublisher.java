package com.notfound.payment.infrastructure.event;

import com.notfound.payment.application.port.out.DepositEventPublisher;
import com.notfound.payment.domain.event.DepositChargedEvent;
import com.notfound.payment.domain.event.DepositDeductedEvent;
import com.notfound.payment.domain.event.DepositRefundedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringDepositEventPublisher implements DepositEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishDepositCharged(DepositChargedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishDepositDeducted(DepositDeductedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishDepositRefunded(DepositRefundedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
