package com.notfound.product.adapter.in.kafka;

import com.notfound.product.adapter.in.kafka.dto.PaymentApprovedEvent;
import com.notfound.product.application.port.in.DeductStockCommand;
import com.notfound.product.application.port.in.DeductStockUseCase;
import com.notfound.product.application.port.out.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentApprovedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentApprovedEventConsumer.class);

    private final DeductStockUseCase deductStockUseCase;
    private final ProcessedEventRepository processedEventRepository;

    public PaymentApprovedEventConsumer(DeductStockUseCase deductStockUseCase,
                                        ProcessedEventRepository processedEventRepository) {
        this.deductStockUseCase = deductStockUseCase;
        this.processedEventRepository = processedEventRepository;
    }

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
