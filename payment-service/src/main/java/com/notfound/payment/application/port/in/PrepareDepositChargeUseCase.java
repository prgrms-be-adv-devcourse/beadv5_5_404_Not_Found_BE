package com.notfound.payment.application.port.in;

import java.util.UUID;

public interface PrepareDepositChargeUseCase {

    PrepareResult prepare(PrepareCommand command);

    record PrepareCommand(UUID memberId, int amount) {}

    record PrepareResult(
            UUID paymentId,
            int amount,
            String pgProvider,
            PgData pgData
    ) {
        public record PgData(
                String clientKey,
                String orderId,
                int amount,
                String orderName,
                String successUrl,
                String failUrl
        ) {}
    }
}
