package com.notfound.payment.application.port.out;

import com.notfound.payment.domain.model.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentPort {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByPgTransactionId(String pgTransactionId);
}
