package com.notfound.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import com.notfound.order.domain.exception.InvalidStateTransitionException;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Test
    @DisplayName("cartItemIds 직렬화 — 정상")
    void serializeCartItemIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        String result = Order.serializeCartItemIds(List.of(id1, id2));
        assertThat(result).isEqualTo(id1 + "," + id2);
    }

    @Test
    @DisplayName("cartItemIds 직렬화 — null/빈 리스트")
    void serializeCartItemIds_empty() {
        assertThat(Order.serializeCartItemIds(null)).isNull();
        assertThat(Order.serializeCartItemIds(List.of())).isNull();
    }

    @Test
    @DisplayName("cartItemIds 파싱 — 정상")
    void parseCartItemIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Order order = Order.builder()
                .cartItemIds(id1 + "," + id2)
                .status(OrderStatus.PENDING)
                .build();

        List<UUID> parsed = order.parseCartItemIds();
        assertThat(parsed).containsExactly(id1, id2);
    }

    @Test
    @DisplayName("cartItemIds 파싱 — null")
    void parseCartItemIds_null() {
        Order order = Order.builder().status(OrderStatus.PENDING).build();
        assertThat(order.parseCartItemIds()).isEmpty();
    }

    @Test
    @DisplayName("cartItemIds 파싱 — 빈 문자열")
    void parseCartItemIds_blank() {
        Order order = Order.builder().cartItemIds("").status(OrderStatus.PENDING).build();
        assertThat(order.parseCartItemIds()).isEmpty();
    }

    @Test
    @DisplayName("cartItemIds 파싱 — 잘못된 UUID 무시")
    void parseCartItemIds_invalidUuid() {
        UUID valid = UUID.randomUUID();
        Order order = Order.builder()
                .cartItemIds(valid + ",invalid-uuid,,  ")
                .status(OrderStatus.PENDING)
                .build();

        List<UUID> parsed = order.parseCartItemIds();
        assertThat(parsed).containsExactly(valid);
    }

    @Test
    @DisplayName("상태 전이 — PENDING → PAID 정상")
    void pay_fromPending() {
        Order order = Order.builder().status(OrderStatus.PENDING).build();
        boolean idempotent = order.pay(50000, "{}");
        assertThat(idempotent).isFalse();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getDepositUsed()).isEqualTo(50000);
    }

    @Test
    @DisplayName("상태 전이 — PAID → PAID 멱등")
    void pay_alreadyPaid() {
        Order order = Order.builder().status(OrderStatus.PAID).depositUsed(50000).build();
        boolean idempotent = order.pay(50000, "{}");
        assertThat(idempotent).isTrue();
    }

    @Test
    @DisplayName("상태 전이 — CANCELLED → PAID 409")
    void pay_fromCancelled() {
        Order order = Order.builder().status(OrderStatus.CANCELLED).build();
        assertThatThrownBy(() -> order.pay(50000, "{}"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    @DisplayName("상태 전이 — CANCELLED → cancel 409")
    void cancel_fromCancelled() {
        Order order = Order.builder().status(OrderStatus.CANCELLED).build();
        assertThatThrownBy(order::cancel)
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
