package com.notfound.product.adapter.in.kafka;

import com.notfound.product.adapter.in.kafka.dto.RefundCompletedEvent;
import com.notfound.product.application.port.in.RestoreStockCommand;
import com.notfound.product.application.port.in.RestoreStockUseCase;
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
class RefundCompletedEventConsumerTest {

    @Mock
    private RestoreStockUseCase restoreStockUseCase;

    @InjectMocks
    private RefundCompletedEventConsumer consumer;

    private UUID orderId;
    private UUID memberId;
    private String eventId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        eventId = UUID.randomUUID().toString();
    }

    private RefundCompletedEvent createEvent(String eventId, List<RefundCompletedEvent.OrderItem> items) {
        return new RefundCompletedEvent(
                eventId,
                "RefundCompletedEvent",
                LocalDateTime.now(),
                new RefundCompletedEvent.Payload(orderId, memberId, items)
        );
    }

    @Nested
    @DisplayName("재고 복원")
    class RestoreStock {

        @Test
        @DisplayName("단일 상품 이벤트 수신 시 커맨드에 eventId와 아이템이 담긴다")
        void success_singleItem() {
            UUID productId = UUID.randomUUID();
            RefundCompletedEvent event = createEvent(eventId,
                    List.of(new RefundCompletedEvent.OrderItem(productId, 2)));

            consumer.consume(event);

            ArgumentCaptor<RestoreStockCommand> captor = ArgumentCaptor.forClass(RestoreStockCommand.class);
            verify(restoreStockUseCase).restoreStock(captor.capture());
            assertThat(captor.getValue().eventId()).isEqualTo(eventId);
            assertThat(captor.getValue().items()).hasSize(1);
            assertThat(captor.getValue().items().get(0).productId()).isEqualTo(productId);
            assertThat(captor.getValue().items().get(0).quantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("여러 상품 이벤트 수신 시 하나의 커맨드로 묶어 전달된다")
        void success_multipleItems() {
            RefundCompletedEvent event = createEvent(eventId, List.of(
                    new RefundCompletedEvent.OrderItem(UUID.randomUUID(), 1),
                    new RefundCompletedEvent.OrderItem(UUID.randomUUID(), 3)
            ));

            consumer.consume(event);

            ArgumentCaptor<RestoreStockCommand> captor = ArgumentCaptor.forClass(RestoreStockCommand.class);
            verify(restoreStockUseCase, times(1)).restoreStock(captor.capture());
            assertThat(captor.getValue().items()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("재고 복원 실패")
    class RestoreStockFailure {

        @Test
        @DisplayName("서비스에서 ProductNotFoundException이 발생하면 전파된다")
        void fail_whenProductNotFound() {
            UUID productId = UUID.randomUUID();
            RefundCompletedEvent event = createEvent(eventId,
                    List.of(new RefundCompletedEvent.OrderItem(productId, 2)));

            doThrow(new ProductNotFoundException(productId))
                    .when(restoreStockUseCase).restoreStock(any());

            assertThatThrownBy(() -> consumer.consume(event))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }
}
