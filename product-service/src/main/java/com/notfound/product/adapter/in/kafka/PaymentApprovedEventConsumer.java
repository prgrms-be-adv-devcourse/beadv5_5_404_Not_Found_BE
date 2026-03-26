package com.notfound.product.adapter.in.kafka;

import com.notfound.product.adapter.in.kafka.dto.PaymentApprovedEvent;
import com.notfound.product.adapter.out.persistence.ProcessedEventJpaEntity;
import com.notfound.product.adapter.out.persistence.ProcessedEventJpaRepository;
import com.notfound.product.application.port.in.DeductStockCommand;
import com.notfound.product.application.port.in.DeductStockUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class PaymentApprovedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentApprovedEventConsumer.class);

    private final DeductStockUseCase deductStockUseCase;
    private final ProcessedEventJpaRepository processedEventRepository;

    public PaymentApprovedEventConsumer(DeductStockUseCase deductStockUseCase,
                                        ProcessedEventJpaRepository processedEventRepository) {
        this.deductStockUseCase = deductStockUseCase;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
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

        processedEventRepository.save(new ProcessedEventJpaEntity(event.eventId(), LocalDateTime.now()));
    }
}
