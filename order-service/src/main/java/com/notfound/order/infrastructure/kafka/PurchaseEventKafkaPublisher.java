package com.notfound.order.infrastructure.kafka;

import com.notfound.order.application.port.out.PurchaseEventPublisher;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class PurchaseEventKafkaPublisher implements PurchaseEventPublisher {

    private static final String TOPIC = "order.purchase-confirmed";
    private static final String TYPE_ID_HEADER = "__TypeId__";
    private static final String EVENT_TYPE = "com.notfound.settlement.adapter.in.kafka.dto.PurchaseConfirmedEvent";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PurchaseEventKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishPurchaseConfirmed(UUID orderId, UUID sellerId, int totalAmount, LocalDateTime confirmedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", orderId.toString());
        payload.put("sellerId", sellerId.toString());
        payload.put("totalAmount", totalAmount);
        payload.put("confirmedAt", confirmedAt != null ? confirmedAt.toString() : null);

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "PurchaseConfirmedEvent");
        event.put("timestamp", Instant.now().toString());
        event.put("payload", payload);

        ProducerRecord<String, Object> record = new ProducerRecord<>(TOPIC, orderId.toString(), event);
        record.headers().add(TYPE_ID_HEADER, EVENT_TYPE.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(record);
    }
}
