package com.notfound.payment.application.port.out;

import com.notfound.payment.domain.model.PgProvider;

import java.time.LocalDateTime;

public interface PgPort {

    PgConfirmResult confirm(PgConfirmCommand command);

    PgCancelResult cancel(PgCancelCommand command);

    record PgConfirmCommand(
            PgProvider pgProvider,
            String paymentKey,
            String orderId,
            int amount
    ) {}

    record PgConfirmResult(
            String pgTransactionId,
            String paymentKey,
            LocalDateTime approvedAt
    ) {}

    record PgCancelCommand(
            PgProvider pgProvider,
            String paymentKey,
            String cancelReason,
            int cancelAmount
    ) {}

    record PgCancelResult(
            String pgRefundId,
            LocalDateTime cancelledAt
    ) {}
}
