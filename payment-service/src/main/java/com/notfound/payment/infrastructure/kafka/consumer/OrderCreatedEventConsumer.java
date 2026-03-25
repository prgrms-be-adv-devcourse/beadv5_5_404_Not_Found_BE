package com.notfound.payment.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.payment.application.port.in.HandleOrderCreatedUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

    private final HandleOrderCreatedUseCase handleOrderCreatedUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.created", groupId = "${spring.application.name}")
    public void consume(String message) {
        try {
            OrderCreatedEventMessage eventMessage = objectMapper.readValue(message, OrderCreatedEventMessage.class);
            handleOrderCreatedUseCase.handle(eventMessage.toCommand());
        } catch (Exception e) {
            log.error("OrderCreatedEvent 처리 실패: {}", e.getMessage(), e);
        }
    }

    record OrderCreatedEventMessage(
            String eventId,
            String eventType,
            String timestamp,
            OrderCreatedPayload payload
    ) {
        HandleOrderCreatedUseCase.OrderCreatedCommand toCommand() {
            return new HandleOrderCreatedUseCase.OrderCreatedCommand(
                    UUID.fromString(eventId),
                    UUID.fromString(payload.orderId()),
                    UUID.fromString(payload.memberId()),
                    payload.totalAmount(),
                    payload.depositUsed(),
                    payload.paymentAmount(),
                    payload.idempotencyKey()
            );
        }
    }

    record OrderCreatedPayload(
            String orderId,
            String memberId,
            int totalAmount,
            int depositUsed,
            int paymentAmount,
            String idempotencyKey
    ) {}
}
