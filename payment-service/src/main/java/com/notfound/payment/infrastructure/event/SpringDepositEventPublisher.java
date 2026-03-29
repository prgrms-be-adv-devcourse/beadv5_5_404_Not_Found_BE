package com.notfound.payment.infrastructure.event;

import com.notfound.payment.application.port.out.DepositEventPublisher;
import com.notfound.payment.domain.event.DepositChargedEvent;
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
}
