package com.notfound.order.infrastructure.kafka;

import com.notfound.order.application.port.out.StockEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class StockEventKafkaPublisher implements StockEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public StockEventKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishStockDeducted(UUID orderId, UUID productId, int quantity) {
        Map<String, Object> event = buildEvent("StockDeductedEvent", orderId, productId, quantity, "ORDER_CREATED");
        kafkaTemplate.send("product.stock-deducted", productId.toString(), event);
    }

    @Override
    public void publishStockRestored(UUID orderId, UUID productId, int quantity) {
        Map<String, Object> event = buildEvent("StockRestoredEvent", orderId, productId, quantity, "ORDER_CANCELLED");
        kafkaTemplate.send("product.stock-restored", productId.toString(), event);
    }

    private Map<String, Object> buildEvent(String eventType, UUID orderId, UUID productId, int quantity, String reason) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", orderId != null ? orderId.toString() : null);
        payload.put("productId", productId.toString());
        payload.put("quantity", quantity);
        payload.put("reason", reason);

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", eventType);
        event.put("timestamp", Instant.now().toString());
        event.put("payload", payload);
        return event;
    }
}
