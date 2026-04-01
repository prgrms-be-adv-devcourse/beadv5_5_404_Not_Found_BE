package com.notfound.order.infrastructure.kafka;

import com.notfound.order.application.port.out.PurchaseEventPublisher;
import com.notfound.order.domain.event.PurchaseConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PurchaseConfirmedEventListener {

    private static final Logger log = LoggerFactory.getLogger(PurchaseConfirmedEventListener.class);

    private final PurchaseEventPublisher purchaseEventPublisher;

    public PurchaseConfirmedEventListener(PurchaseEventPublisher purchaseEventPublisher) {
        this.purchaseEventPublisher = purchaseEventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PurchaseConfirmedEvent event) {
        try {
            purchaseEventPublisher.publishPurchaseConfirmed(
                    event.eventId(), event.orderId(), event.sellerId(),
                    event.totalAmount(), event.confirmedAt());
            log.info("Kafka 발행 완료: orderId={}, eventId={}", event.orderId(), event.eventId());
        } catch (Exception e) {
            log.error("Kafka 발행 실패: orderId={}, eventId={}, cause={}",
                    event.orderId(), event.eventId(), e.getMessage(), e);
        }
    }
}
