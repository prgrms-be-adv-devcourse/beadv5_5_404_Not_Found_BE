package com.notfound.payment.infrastructure.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.payment.application.port.out.PaymentEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class PaymentKafkaEventAdapter implements PaymentEventPort {

    private static final String TOPIC_PAYMENT_APPROVED = "payment.approved";
    private static final String TOPIC_PAYMENT_FAILED = "payment.failed";
    private static final String TOPIC_REFUND_COMPLETED = "refund.completed";
    private static final String TOPIC_REFUND_FAILED = "refund.failed";
    private static final String TOPIC_DEPOSIT_CHANGED = "deposit.changed";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publishPaymentApproved(PaymentApprovedEvent event) {
        send(TOPIC_PAYMENT_APPROVED, event.paymentId().toString(), "PaymentApprovedEvent", event);
    }

    @Override
    public void publishPaymentFailed(PaymentFailedEvent event) {
        send(TOPIC_PAYMENT_FAILED, event.paymentId().toString(), "PaymentFailedEvent", event);
    }

    @Override
    public void publishRefundCompleted(RefundCompletedEvent event) {
        send(TOPIC_REFUND_COMPLETED, event.refundId().toString(), "RefundCompletedEvent", event);
    }

    @Override
    public void publishRefundFailed(RefundFailedEvent event) {
        send(TOPIC_REFUND_FAILED, event.refundId().toString(), "RefundFailedEvent", event);
    }

    @Override
    public void publishDepositChanged(DepositChangedEvent event) {
        send(TOPIC_DEPOSIT_CHANGED, event.memberId().toString(), "DepositChangedEvent", event);
    }

    private void send(String topic, String key, String eventType, Object payload) {
        try {
            KafkaMessage message = new KafkaMessage(
                    UUID.randomUUID().toString(),
                    eventType,
                    LocalDateTime.now().toString(),
                    payload
            );
            kafkaTemplate.send(topic, key, objectMapper.writeValueAsString(message));
            log.debug("Kafka 이벤트 발행: topic={}, eventType={}, key={}", topic, eventType, key);
        } catch (JsonProcessingException e) {
            log.error("Kafka 이벤트 직렬화 실패: topic={}, eventType={}", topic, eventType, e);
            throw new RuntimeException("Kafka 이벤트 직렬화 실패", e);
        }
    }

    private record KafkaMessage(String eventId, String eventType, String timestamp, Object payload) {}
}
