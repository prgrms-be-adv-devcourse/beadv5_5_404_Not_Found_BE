package com.notfound.product.adapter.in.kafka;

import com.notfound.product.adapter.in.kafka.dto.RefundCompletedEvent;
import com.notfound.product.application.port.in.RestoreStockCommand;
import com.notfound.product.application.port.in.RestoreStockUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundCompletedEventConsumer {

    private final RestoreStockUseCase restoreStockUseCase;

    @KafkaListener(topics = "refund.completed", groupId = "product-service")
    public void consume(RefundCompletedEvent event) {
        log.info("RefundCompletedEvent 수신 — orderId: {}", event.payload().orderId());

        List<RestoreStockCommand.StockItem> items = event.payload().orderItems().stream()
                .map(item -> new RestoreStockCommand.StockItem(item.productId(), item.quantity()))
                .toList();
        restoreStockUseCase.restoreStock(new RestoreStockCommand(event.eventId(), items));
    }
}
