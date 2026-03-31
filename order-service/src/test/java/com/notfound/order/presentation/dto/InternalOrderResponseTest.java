package com.notfound.order.presentation.dto;

import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderItem;
import com.notfound.order.domain.model.OrderItemStatus;
import com.notfound.order.domain.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InternalOrderResponseTest {

    private static final UUID ORDER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID PRODUCT_ID_1 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID PRODUCT_ID_2 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @Test
    @DisplayName("Order + List<OrderItem> → InternalOrderResponse 변환 정확성")
    void from_convertsCorrectly() {
        Order order = Order.builder()
                .id(ORDER_ID)
                .memberId(UUID.randomUUID())
                .status(OrderStatus.PENDING)
                .totalAmount(35000)
                .build();

        List<OrderItem> items = List.of(
                OrderItem.builder()
                        .orderId(ORDER_ID)
                        .productId(PRODUCT_ID_1)
                        .quantity(2)
                        .unitPrice(10000)
                        .subtotal(20000)
                        .status(OrderItemStatus.PAID)
                        .build(),
                OrderItem.builder()
                        .orderId(ORDER_ID)
                        .productId(PRODUCT_ID_2)
                        .quantity(3)
                        .unitPrice(5000)
                        .subtotal(15000)
                        .status(OrderItemStatus.PAID)
                        .build()
        );

        InternalOrderResponse response = InternalOrderResponse.from(order, items);

        assertThat(response.orderId()).isEqualTo(ORDER_ID);
        assertThat(response.totalAmount()).isEqualTo(35000);
        assertThat(response.items()).hasSize(2);
    }

    @Test
    @DisplayName("productId, quantity 매핑 검증")
    void from_mapsProductIdAndQuantity() {
        Order order = Order.builder()
                .id(ORDER_ID)
                .totalAmount(25000)
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> items = List.of(
                OrderItem.builder()
                        .productId(PRODUCT_ID_1)
                        .quantity(5)
                        .unitPrice(3000)
                        .subtotal(15000)
                        .status(OrderItemStatus.PAID)
                        .build(),
                OrderItem.builder()
                        .productId(PRODUCT_ID_2)
                        .quantity(2)
                        .unitPrice(5000)
                        .subtotal(10000)
                        .status(OrderItemStatus.PAID)
                        .build()
        );

        InternalOrderResponse response = InternalOrderResponse.from(order, items);

        assertThat(response.items().get(0).productId()).isEqualTo(PRODUCT_ID_1);
        assertThat(response.items().get(0).quantity()).isEqualTo(5);
        assertThat(response.items().get(1).productId()).isEqualTo(PRODUCT_ID_2);
        assertThat(response.items().get(1).quantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("빈 OrderItem 리스트 → 빈 items 반환")
    void from_emptyItems() {
        Order order = Order.builder()
                .id(ORDER_ID)
                .totalAmount(0)
                .status(OrderStatus.PENDING)
                .build();

        InternalOrderResponse response = InternalOrderResponse.from(order, List.of());

        assertThat(response.orderId()).isEqualTo(ORDER_ID);
        assertThat(response.items()).isEmpty();
    }
}
