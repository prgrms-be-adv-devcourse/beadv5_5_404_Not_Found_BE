package com.notfound.payment.application.service;

import com.notfound.payment.application.port.in.DeductDepositUseCase;
import com.notfound.payment.application.port.in.PayOrderUseCase;
import com.notfound.payment.application.port.in.RefundDepositUseCase;
import com.notfound.payment.application.port.out.OrderPort;
import com.notfound.payment.application.port.out.ProductPort;
import com.notfound.payment.domain.exception.PaymentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PayOrderServiceTest {

    @Mock private OrderPort orderPort;
    @Mock private ProductPort productPort;
    @Mock private DeductDepositUseCase deductDepositUseCase;
    @Mock private RefundDepositUseCase refundDepositUseCase;

    @InjectMocks
    private PayOrderService payOrderService;

    @Test
    void pay_이미_PAID_주문이면_예외() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        given(orderPort.getOrder(orderId)).willReturn(
                new OrderPort.OrderDetail(orderId, "PAID", 25000, List.of())
        );

        assertThatThrownBy(() -> payOrderService.pay(new PayOrderUseCase.PayCommand(memberId, orderId)))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("이미 결제된");

        then(deductDepositUseCase).shouldHaveNoInteractions();
        then(productPort).shouldHaveNoInteractions();
    }

    @Test
    void pay_예치금_부족이면_예외_및_재고_차감_안_함() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        given(orderPort.getOrder(orderId)).willReturn(
                new OrderPort.OrderDetail(orderId, "PENDING", 25000,
                        List.of(new OrderPort.OrderItem(UUID.randomUUID(), 1)))
        );
        given(deductDepositUseCase.deduct(any())).willThrow(PaymentException.depositInsufficientBalance());

        assertThatThrownBy(() -> payOrderService.pay(new PayOrderUseCase.PayCommand(memberId, orderId)))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("잔액이 부족");

        then(productPort).shouldHaveNoInteractions();
        then(orderPort).should(never()).updateOrderStatus(any(), any(), any(int.class));
    }

    @Test
    void pay_재고_차감_실패시_예치금_복원_후_예외() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        given(orderPort.getOrder(orderId)).willReturn(
                new OrderPort.OrderDetail(orderId, "PENDING", 25000,
                        List.of(new OrderPort.OrderItem(UUID.randomUUID(), 2)))
        );
        given(deductDepositUseCase.deduct(any())).willReturn(
                new DeductDepositUseCase.DeductResult(UUID.randomUUID(), 75000)
        );
        willThrow(new RuntimeException("재고 없음")).given(productPort).deductStock(any());

        assertThatThrownBy(() -> payOrderService.pay(new PayOrderUseCase.PayCommand(memberId, orderId)))
                .isInstanceOf(RuntimeException.class);

        then(refundDepositUseCase).should().refund(any());
        then(orderPort).should(never()).updateOrderStatus(any(), any(), any(int.class));
    }

    @Test
    void pay_정상_결제시_순서대로_처리하고_결과_반환() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        given(orderPort.getOrder(orderId)).willReturn(
                new OrderPort.OrderDetail(orderId, "PENDING", 25000,
                        List.of(new OrderPort.OrderItem(productId, 1)))
        );
        given(deductDepositUseCase.deduct(any())).willReturn(
                new DeductDepositUseCase.DeductResult(UUID.randomUUID(), 75000)
        );

        PayOrderUseCase.PayResult result = payOrderService.pay(new PayOrderUseCase.PayCommand(memberId, orderId));

        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.depositUsed()).isEqualTo(25000);
        assertThat(result.balanceAfter()).isEqualTo(75000);
        then(productPort).should().deductStock(any());
        then(orderPort).should().updateOrderStatus(orderId, "PAID", 25000);
    }

    @Test
    void pay_정상_결제시_refund_호출_안_함() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        given(orderPort.getOrder(orderId)).willReturn(
                new OrderPort.OrderDetail(orderId, "PENDING", 25000,
                        List.of(new OrderPort.OrderItem(UUID.randomUUID(), 1)))
        );
        given(deductDepositUseCase.deduct(any())).willReturn(
                new DeductDepositUseCase.DeductResult(UUID.randomUUID(), 75000)
        );

        payOrderService.pay(new PayOrderUseCase.PayCommand(memberId, orderId));

        then(refundDepositUseCase).shouldHaveNoInteractions();
    }
}
