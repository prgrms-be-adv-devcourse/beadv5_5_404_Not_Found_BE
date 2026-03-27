package com.notfound.product.adapter.in.kafka;

import com.notfound.product.adapter.in.kafka.dto.PaymentApprovedEvent;
import com.notfound.product.application.port.in.DeductStockCommand;
import com.notfound.product.application.port.in.DeductStockUseCase;
import com.notfound.product.domain.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentApprovedEventConsumerTest {

    @Mock
    private DeductStockUseCase deductStockUseCase;

    @InjectMocks
    private PaymentApprovedEventConsumer consumer;

    private UUID orderId;
    private UUID memberId;
    private String eventId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        eventId = UUID.randomUUID().toString();
    }

    private PaymentApprovedEvent createEvent(String eventId, List<PaymentApprovedEvent.OrderItem> items) {
        return new PaymentApprovedEvent(
                eventId,
                "PaymentApprovedEvent",
                LocalDateTime.now(),
                new PaymentApprovedEvent.Payload(orderId, memberId, items)
        );
    }

    @Nested
    @DisplayName("재고 차감")
    class DeductStock {

        @Test
        @DisplayName("단일 상품 이벤트 수신 시 커맨드에 eventId와 아이템이 담긴다")
        void success_singleItem() {
            UUID productId = UUID.randomUUID();
            PaymentApprovedEvent event = createEvent(eventId,
                    List.of(new PaymentApprovedEvent.OrderItem(productId, 2)));

            consumer.consume(event);

            ArgumentCaptor<DeductStockCommand> captor = ArgumentCaptor.forClass(DeductStockCommand.class);
            verify(deductStockUseCase).deductStock(captor.capture());
            assertThat(captor.getValue().eventId()).isEqualTo(eventId);
            assertThat(captor.getValue().items()).hasSize(1);
            assertThat(captor.getValue().items().get(0).productId()).isEqualTo(productId);
            assertThat(captor.getValue().items().get(0).quantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("여러 상품 이벤트 수신 시 하나의 커맨드로 묶어 전달된다")
        void success_multipleItems() {
            PaymentApprovedEvent event = createEvent(eventId, List.of(
                    new PaymentApprovedEvent.OrderItem(UUID.randomUUID(), 1),
                    new PaymentApprovedEvent.OrderItem(UUID.randomUUID(), 3),
                    new PaymentApprovedEvent.OrderItem(UUID.randomUUID(), 2)
            ));

            consumer.consume(event);

            ArgumentCaptor<DeductStockCommand> captor = ArgumentCaptor.forClass(DeductStockCommand.class);
            verify(deductStockUseCase, times(1)).deductStock(captor.capture());
            assertThat(captor.getValue().items()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("재고 차감 실패")
    class DeductStockFailure {

        @Test
        @DisplayName("서비스에서 ProductNotFoundException이 발생하면 전파된다")
        void fail_whenProductNotFound() {
            UUID productId = UUID.randomUUID();
            PaymentApprovedEvent event = createEvent(eventId,
                    List.of(new PaymentApprovedEvent.OrderItem(productId, 2)));

            doThrow(new ProductNotFoundException(productId))
                    .when(deductStockUseCase).deductStock(any());

            assertThatThrownBy(() -> consumer.consume(event))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }
}
