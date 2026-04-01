package com.notfound.order.application.service;

import com.notfound.order.application.port.in.GetInternalOrderUseCase.InternalOrderDetail;
import com.notfound.order.application.port.out.*;
import com.notfound.order.domain.exception.OrderException;
import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderItem;
import com.notfound.order.domain.model.OrderItemStatus;
import com.notfound.order.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetInternalOrderUseCaseTest {

    private OrderService orderService;
    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;

    private static final UUID ORDER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID PRODUCT_ID_1 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID PRODUCT_ID_2 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        orderService = new OrderService(
                orderRepository,
                orderItemRepository,
                mock(CartRepository.class),
                mock(CartItemRepository.class),
                mock(MemberServicePort.class),
                mock(ProductServicePort.class),
                mock(PurchaseEventPublisher.class)
        );
    }

    @Test
    @DisplayName("정상 조회 — 존재하는 orderId → OrderDetail 반환, totalAmount와 items 검증")
    void getOrder_success() {
        Order order = Order.builder()
                .id(ORDER_ID)
                .memberId(UUID.randomUUID())
                .status(OrderStatus.PENDING)
                .totalAmount(25000)
                .build();
        List<OrderItem> items = List.of(
                OrderItem.builder()
                        .id(UUID.randomUUID())
                        .orderId(ORDER_ID)
                        .productId(PRODUCT_ID_1)
                        .quantity(2)
                        .unitPrice(10000)
                        .subtotal(20000)
                        .status(OrderItemStatus.PAID)
                        .build(),
                OrderItem.builder()
                        .id(UUID.randomUUID())
                        .orderId(ORDER_ID)
                        .productId(PRODUCT_ID_2)
                        .quantity(1)
                        .unitPrice(5000)
                        .subtotal(5000)
                        .status(OrderItemStatus.PAID)
                        .build()
        );

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(items);

        InternalOrderDetail result = orderService.getOrder(ORDER_ID);

        assertThat(result.order().getId()).isEqualTo(ORDER_ID);
        assertThat(result.order().getTotalAmount()).isEqualTo(25000);
        assertThat(result.orderItems()).hasSize(2);
        assertThat(result.orderItems().get(0).getProductId()).isEqualTo(PRODUCT_ID_1);
        assertThat(result.orderItems().get(1).getProductId()).isEqualTo(PRODUCT_ID_2);
    }

    @Test
    @DisplayName("존재하지 않는 orderId → OrderException 발생 (ORDER_NOT_FOUND)")
    void getOrder_notFound() {
        UUID nonExistentId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(nonExistentId))
                .isInstanceOf(OrderException.class)
                .satisfies(ex -> assertThat(((OrderException) ex).getCode()).isEqualTo("ORDER_NOT_FOUND"));
    }

    @Test
    @DisplayName("주문은 있으나 OrderItem이 0건 → 빈 items 리스트 반환")
    void getOrder_emptyItems() {
        Order order = Order.builder()
                .id(ORDER_ID)
                .memberId(UUID.randomUUID())
                .status(OrderStatus.PENDING)
                .totalAmount(0)
                .build();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of());

        InternalOrderDetail result = orderService.getOrder(ORDER_ID);

        assertThat(result.order().getId()).isEqualTo(ORDER_ID);
        assertThat(result.orderItems()).isEmpty();
    }

    @Test
    @DisplayName("OrderItem이 여러 건 → 전체 items 정상 반환")
    void getOrder_multipleItems() {
        Order order = Order.builder()
                .id(ORDER_ID)
                .memberId(UUID.randomUUID())
                .status(OrderStatus.PENDING)
                .totalAmount(55000)
                .build();
        List<OrderItem> items = List.of(
                OrderItem.builder().orderId(ORDER_ID).productId(PRODUCT_ID_1)
                        .quantity(3).unitPrice(10000).subtotal(30000).status(OrderItemStatus.PAID).build(),
                OrderItem.builder().orderId(ORDER_ID).productId(PRODUCT_ID_2)
                        .quantity(1).unitPrice(15000).subtotal(15000).status(OrderItemStatus.PAID).build(),
                OrderItem.builder().orderId(ORDER_ID).productId(UUID.randomUUID())
                        .quantity(2).unitPrice(5000).subtotal(10000).status(OrderItemStatus.PAID).build()
        );

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(items);

        InternalOrderDetail result = orderService.getOrder(ORDER_ID);

        assertThat(result.orderItems()).hasSize(3);
        assertThat(result.orderItems())
                .extracting(OrderItem::getQuantity)
                .containsExactly(3, 1, 2);
    }
}
