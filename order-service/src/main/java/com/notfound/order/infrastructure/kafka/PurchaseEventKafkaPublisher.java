package com.notfound.order.infrastructure.kafka;

import com.notfound.order.application.port.out.PurchaseEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class PurchaseEventKafkaPublisher implements PurchaseEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PurchaseEventKafkaPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PurchaseEventKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishPurchaseConfirmed(UUID orderId, UUID memberId, int totalAmount, LocalDateTime confirmedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", orderId.toString());
        payload.put("memberId", memberId.toString());
        payload.put("totalAmount", totalAmount);
        payload.put("confirmedAt", confirmedAt != null ? confirmedAt.toString() : null);

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "PurchaseConfirmedEvent");
        event.put("timestamp", Instant.now().toString());
        event.put("payload", payload);

        kafkaTemplate.send("order.purchase-confirmed", orderId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] PurchaseConfirmedEvent 발행 실패: orderId={}, cause={}", orderId, ex.getMessage(), ex);
                    }
                });
    }
}
