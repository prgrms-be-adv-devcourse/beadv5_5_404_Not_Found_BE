package com.notfound.product.adapter.in.kafka;

import com.notfound.product.adapter.in.kafka.dto.PaymentApprovedEvent;
import com.notfound.product.application.port.in.DeductStockCommand;
import com.notfound.product.application.port.in.DeductStockUseCase;
import com.notfound.product.application.port.out.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentApprovedEventConsumer {

    private final DeductStockUseCase deductStockUseCase;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "payment.approved", groupId = "product-service")
    public void consume(PaymentApprovedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.warn("중복 이벤트 무시 — eventId: {}", event.eventId());
            return;
        }

        log.info("PaymentApprovedEvent 수신 — orderId: {}", event.payload().orderId());

        for (PaymentApprovedEvent.OrderItem item : event.payload().orderItems()) {
            deductStockUseCase.deductStock(new DeductStockCommand(item.productId(), item.quantity()));
        }

        processedEventRepository.save(event.eventId());
    }
}
