package com.notfound.product.adapter.in.kafka;

import com.notfound.product.adapter.in.kafka.dto.PaymentApprovedEvent;
import com.notfound.product.application.port.in.DeductStockCommand;
import com.notfound.product.application.port.in.DeductStockUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentApprovedEventConsumer {

    private final DeductStockUseCase deductStockUseCase;

    @KafkaListener(topics = "payment.approved", groupId = "product-service")
    public void consume(PaymentApprovedEvent event) {
        log.info("PaymentApprovedEvent 수신 — orderId: {}", event.payload().orderId());

        List<DeductStockCommand.StockItem> items = event.payload().orderItems().stream()
                .map(item -> new DeductStockCommand.StockItem(item.productId(), item.quantity()))
                .toList();
        deductStockUseCase.deductStock(new DeductStockCommand(event.eventId(), items));
    }
}
