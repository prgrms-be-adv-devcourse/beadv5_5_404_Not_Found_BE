package com.notfound.payment.application.service;

import com.notfound.payment.application.port.in.DeductDepositUseCase;
import com.notfound.payment.application.port.in.PayOrderUseCase;
import com.notfound.payment.application.port.in.RefundDepositUseCase;
import com.notfound.payment.application.port.out.OrderPort;
import com.notfound.payment.application.port.out.ProductPort;
import com.notfound.payment.domain.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayOrderService implements PayOrderUseCase {

    private final OrderPort orderPort;
    private final ProductPort productPort;
    private final DeductDepositUseCase deductDepositUseCase;
    private final RefundDepositUseCase refundDepositUseCase;

    @Override
    public PayResult pay(PayCommand command) {
        // 1. 주문 조회 (총액 + 상품 목록)
        OrderPort.OrderDetail order = orderPort.getOrder(command.orderId());

        if ("PAID".equals(order.status())) {
            throw PaymentException.orderAlreadyPaid(command.orderId());
        }

        List<ProductPort.StockItem> stockItems = order.items().stream()
                .map(item -> new ProductPort.StockItem(item.productId(), item.quantity()))
                .toList();

        // 2. 예치금 차감 (잔액 부족 시 즉시 예외 — 재고 차감 전이므로 보상 불필요)
        DeductDepositUseCase.DeductResult deductResult = deductDepositUseCase.deduct(
                new DeductDepositUseCase.DeductCommand(
                        command.memberId(),
                        command.orderId(),
                        order.totalAmount(),
                        "주문 결제 - " + command.orderId()
                )
        );

        // 3. 재고 차감 (실패 시 예치금 복원 후 예외)
        try {
            productPort.deductStock(stockItems);
        } catch (Exception e) {
            log.warn("재고 차감 실패 — orderId={}, 예치금 복원 시작", command.orderId());
            refundDepositUseCase.refund(
                    new RefundDepositUseCase.RefundCommand(
                            command.memberId(),
                            command.orderId(),
                            order.totalAmount(),
                            "재고 부족으로 인한 결제 취소 - " + command.orderId()
                    )
            );
            throw e;
        }

        // 4. 주문 상태 PENDING → PAID
        orderPort.updateOrderStatus(command.orderId(), "PAID", order.totalAmount());

        return new PayResult(command.orderId(), order.totalAmount(), deductResult.balanceAfter());
    }
}
