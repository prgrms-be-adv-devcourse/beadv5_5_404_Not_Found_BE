package com.notfound.payment.application.port.out;

import com.notfound.payment.domain.model.DepositType;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PaymentEventPort {

    void publishPaymentApproved(PaymentApprovedEvent event);

    void publishPaymentFailed(PaymentFailedEvent event);

    void publishRefundCompleted(RefundCompletedEvent event);

    void publishRefundFailed(RefundFailedEvent event);

    void publishDepositChanged(DepositChangedEvent event);

    record PaymentApprovedEvent(
            UUID paymentId,
            UUID orderId,
            UUID memberId,
            int amount,
            LocalDateTime paidAt
    ) {}

    record PaymentFailedEvent(
            UUID paymentId,
            UUID orderId,
            UUID memberId
    ) {}

    record RefundCompletedEvent(
            UUID refundId,
            UUID paymentId,
            UUID orderId,
            UUID orderItemId,
            int amount,
            LocalDateTime refundedAt
    ) {}

    record RefundFailedEvent(
            UUID refundId,
            UUID paymentId,
            UUID orderId,
            UUID orderItemId
    ) {}

    /**
     * 예치금 변동 이벤트 (fix 8).
     * Payment 서비스가 발행하면 Member 서비스가 구독해 deposit_balance를 갱신한다.
     * Topic: deposit.changed
     */
    record DepositChangedEvent(
            UUID memberId,
            DepositType changeType,
            int changeAmount,
            int balanceAfter
    ) {}
}
