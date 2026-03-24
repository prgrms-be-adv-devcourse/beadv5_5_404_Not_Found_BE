package com.notfound.payment.infrastructure.persistence;

import com.notfound.payment.application.port.out.RefundPort;
import com.notfound.payment.domain.model.Refund;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefundRepositoryAdapter implements RefundPort {

    private final RefundJpaRepository refundJpaRepository;

    @Override
    public Refund save(Refund refund) {
        return refundJpaRepository.save(RefundJpaEntity.fromModel(refund)).toModel();
    }

    @Override
    public Optional<Refund> findById(UUID id) {
        return refundJpaRepository.findById(id).map(RefundJpaEntity::toModel);
    }

    @Override
    public List<Refund> findByPaymentId(UUID paymentId) {
        return refundJpaRepository.findByPaymentId(paymentId).stream()
                .map(RefundJpaEntity::toModel)
                .toList();
    }

    @Override
    public List<Refund> findByOrderItemId(UUID orderItemId) {
        return refundJpaRepository.findByOrderItemId(orderItemId).stream()
                .map(RefundJpaEntity::toModel)
                .toList();
    }
}
