package com.notfound.payment.infrastructure.persistence;

import com.notfound.payment.application.port.out.PaymentPort;
import com.notfound.payment.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentPort {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(PaymentJpaEntity.fromModel(payment)).toModel();
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return paymentJpaRepository.findById(id).map(PaymentJpaEntity::toModel);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId).map(PaymentJpaEntity::toModel);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return paymentJpaRepository.findByIdempotencyKey(idempotencyKey).map(PaymentJpaEntity::toModel);
    }

    @Override
    public Optional<Payment> findByPgTransactionId(String pgTransactionId) {
        return paymentJpaRepository.findByPgTransactionId(pgTransactionId).map(PaymentJpaEntity::toModel);
    }
}
