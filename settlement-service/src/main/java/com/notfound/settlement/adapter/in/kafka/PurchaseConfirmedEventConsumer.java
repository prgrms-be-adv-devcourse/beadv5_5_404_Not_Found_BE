package com.notfound.settlement.adapter.in.kafka;

import com.notfound.settlement.adapter.in.kafka.dto.PurchaseConfirmedEvent;
import com.notfound.settlement.application.port.in.CreateSettlementTargetCommand;
import com.notfound.settlement.application.port.in.CreateSettlementTargetUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PurchaseConfirmedEventConsumer {

    private final CreateSettlementTargetUseCase createSettlementTargetUseCase;

    @KafkaListener(topics = "order.purchase-confirmed", groupId = "settlement-service")
    public void consume(PurchaseConfirmedEvent event) {
        log.info("PurchaseConfirmedEvent 수신 — orderId: {}, sellerId: {}",
                event.payload().orderId(), event.payload().sellerId());

        createSettlementTargetUseCase.createSettlementTarget(
                new CreateSettlementTargetCommand(
                        event.eventId(),
                        event.payload().orderId(),
                        event.payload().sellerId(),
                        event.payload().totalAmount(),
                        event.timestamp()   // confirmedAt = 이벤트 발행 시각 (이슈 #27)
                )
        );
    }
}
