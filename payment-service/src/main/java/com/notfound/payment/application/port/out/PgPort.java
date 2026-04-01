package com.notfound.payment.application.port.out;

import java.time.LocalDateTime;

public interface PgPort {

    PgConfirmResult confirm(PgConfirmCommand command);

    PgCancelResult cancel(PgCancelCommand command);

    PgConfig getConfig();

    record PgConfig(String clientKey, String successUrl, String failUrl) {}

    record PgConfirmCommand(
            String paymentKey,
            String orderId,
            int amount
    ) {}

    record PgConfirmResult(
            String pgTransactionId,
            String paymentKey,
            LocalDateTime approvedAt,
            String method
    ) {}

    record PgCancelCommand(
            String paymentKey,
            String cancelReason,
            int cancelAmount
    ) {}

    record PgCancelResult(
            String pgRefundId,
            LocalDateTime cancelledAt
    ) {}
}
