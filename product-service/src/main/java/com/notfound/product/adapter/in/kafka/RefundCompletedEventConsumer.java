package com.notfound.product.adapter.in.kafka;

import com.notfound.product.adapter.in.kafka.dto.RefundCompletedEvent;
import com.notfound.product.adapter.out.persistence.ProcessedEventJpaEntity;
import com.notfound.product.adapter.out.persistence.ProcessedEventJpaRepository;
import com.notfound.product.application.port.in.RestoreStockCommand;
import com.notfound.product.application.port.in.RestoreStockUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class RefundCompletedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(RefundCompletedEventConsumer.class);

    private final RestoreStockUseCase restoreStockUseCase;
    private final ProcessedEventJpaRepository processedEventRepository;

    public RefundCompletedEventConsumer(RestoreStockUseCase restoreStockUseCase,
                                        ProcessedEventJpaRepository processedEventRepository) {
        this.restoreStockUseCase = restoreStockUseCase;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    @KafkaListener(topics = "refund.completed", groupId = "product-service")
    public void consume(RefundCompletedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.warn("중복 이벤트 무시 — eventId: {}", event.eventId());
            return;
        }

        log.info("RefundCompletedEvent 수신 — orderId: {}", event.payload().orderId());

        for (RefundCompletedEvent.OrderItem item : event.payload().orderItems()) {
            restoreStockUseCase.restoreStock(new RestoreStockCommand(item.productId(), item.quantity()));
        }

        processedEventRepository.save(new ProcessedEventJpaEntity(event.eventId(), LocalDateTime.now()));
    }
}
