package com.notfound.member.application.listener;

import com.notfound.member.application.port.in.ChargeDepositUseCase;
import com.notfound.member.application.port.in.DeductDepositUseCase;
import com.notfound.member.domain.event.DepositChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DepositChangedEventListener {

    private static final Logger log = LoggerFactory.getLogger(DepositChangedEventListener.class);

    private final ChargeDepositUseCase chargeDepositUseCase;
    private final DeductDepositUseCase deductDepositUseCase;

    public DepositChangedEventListener(ChargeDepositUseCase chargeDepositUseCase,
                                       DeductDepositUseCase deductDepositUseCase) {
        this.chargeDepositUseCase = chargeDepositUseCase;
        this.deductDepositUseCase = deductDepositUseCase;
    }

    @EventListener
    public void handle(DepositChangedEvent event) {
        log.info("예치금 변경 이벤트 수신: memberId={}, type={}, amount={}",
                event.memberId(), event.type(), event.amount());

        switch (event.type()) {
            case "CHARGE" -> chargeDepositUseCase.chargeDeposit(event.memberId(), event.amount());
            case "DEDUCT" -> deductDepositUseCase.deductDeposit(event.memberId(), event.amount());
            default -> log.warn("알 수 없는 예치금 변경 타입: {}", event.type());
        }
    }
}
