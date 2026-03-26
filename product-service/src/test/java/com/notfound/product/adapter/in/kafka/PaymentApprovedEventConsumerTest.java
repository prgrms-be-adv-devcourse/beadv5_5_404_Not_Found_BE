package com.notfound.product.adapter.in.kafka;

import com.notfound.product.adapter.in.kafka.dto.PaymentApprovedEvent;
import com.notfound.product.adapter.out.persistence.ProcessedEventJpaEntity;
import com.notfound.product.adapter.out.persistence.ProcessedEventJpaRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentApprovedEventConsumerTest {

    @Mock
    private DeductStockUseCase deductStockUseCase;

    @Mock
    private ProcessedEventJpaRepository processedEventRepository;

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
        @DisplayName("단일 상품 이벤트 수신 시 재고가 차감된다")
        void success_singleItem() {
            UUID productId = UUID.randomUUID();
            PaymentApprovedEvent event = createEvent(eventId,
                    List.of(new PaymentApprovedEvent.OrderItem(productId, 2)));

            given(processedEventRepository.existsById(eventId)).willReturn(false);

            consumer.consume(event);

            ArgumentCaptor<DeductStockCommand> captor = ArgumentCaptor.forClass(DeductStockCommand.class);
            verify(deductStockUseCase).deductStock(captor.capture());
            assertThat(captor.getValue().productId()).isEqualTo(productId);
            assertThat(captor.getValue().quantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("여러 상품 이벤트 수신 시 각 상품의 재고가 모두 차감된다")
        void success_multipleItems() {
            UUID productId1 = UUID.randomUUID();
            UUID productId2 = UUID.randomUUID();
            UUID productId3 = UUID.randomUUID();
            PaymentApprovedEvent event = createEvent(eventId, List.of(
                    new PaymentApprovedEvent.OrderItem(productId1, 1),
                    new PaymentApprovedEvent.OrderItem(productId2, 3),
                    new PaymentApprovedEvent.OrderItem(productId3, 2)
            ));

            given(processedEventRepository.existsById(eventId)).willReturn(false);

            consumer.consume(event);

            verify(deductStockUseCase, times(3)).deductStock(any());
        }

        @Test
        @DisplayName("재고 차감 후 eventId가 저장된다")
        void success_savesProcessedEvent() {
            UUID productId = UUID.randomUUID();
            PaymentApprovedEvent event = createEvent(eventId,
                    List.of(new PaymentApprovedEvent.OrderItem(productId, 1)));

            given(processedEventRepository.existsById(eventId)).willReturn(false);

            consumer.consume(event);

            ArgumentCaptor<ProcessedEventJpaEntity> captor = ArgumentCaptor.forClass(ProcessedEventJpaEntity.class);
            verify(processedEventRepository).save(captor.capture());
        }
    }

    @Nested
    @DisplayName("중복 이벤트 처리")
    class DuplicateEvent {

        @Test
        @DisplayName("이미 처리된 eventId면 재고 차감을 수행하지 않는다")
        void skip_whenAlreadyProcessed() {
            PaymentApprovedEvent event = createEvent(eventId,
                    List.of(new PaymentApprovedEvent.OrderItem(UUID.randomUUID(), 2)));

            given(processedEventRepository.existsById(eventId)).willReturn(true);

            consumer.consume(event);

            verify(deductStockUseCase, never()).deductStock(any());
            verify(processedEventRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("재고 차감 실패")
    class DeductStockFailure {

        @Test
        @DisplayName("존재하지 않는 상품이면 ProductNotFoundException이 발생한다")
        void fail_whenProductNotFound() {
            UUID productId = UUID.randomUUID();
            PaymentApprovedEvent event = createEvent(eventId,
                    List.of(new PaymentApprovedEvent.OrderItem(productId, 2)));

            given(processedEventRepository.existsById(eventId)).willReturn(false);
            doThrow(new ProductNotFoundException(productId))
                    .when(deductStockUseCase).deductStock(any());

            assertThatThrownBy(() -> consumer.consume(event))
                    .isInstanceOf(ProductNotFoundException.class);

            verify(processedEventRepository, never()).save(any());
        }
    }
}
