package com.notfound.product.adapter.in.kafka;

import com.notfound.product.adapter.in.kafka.dto.RefundCompletedEvent;
import com.notfound.product.application.port.in.RestoreStockCommand;
import com.notfound.product.application.port.in.RestoreStockUseCase;

import java.util.List;
import com.notfound.product.application.port.out.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundCompletedEventConsumer {

    private final RestoreStockUseCase restoreStockUseCase;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "refund.completed", groupId = "product-service")
    public void consume(RefundCompletedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.warn("중복 이벤트 무시 — eventId: {}", event.eventId());
            return;
        }

        log.info("RefundCompletedEvent 수신 — orderId: {}", event.payload().orderId());

        List<RestoreStockCommand.StockItem> items = event.payload().orderItems().stream()
                .map(item -> new RestoreStockCommand.StockItem(item.productId(), item.quantity()))
                .toList();
        restoreStockUseCase.restoreStock(new RestoreStockCommand(items));

        processedEventRepository.save(event.eventId());
    }
}
