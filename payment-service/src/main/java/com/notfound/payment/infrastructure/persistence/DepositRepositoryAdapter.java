package com.notfound.payment.infrastructure.persistence;

import com.notfound.payment.application.port.out.DepositPort;
import com.notfound.payment.domain.model.Deposit;
import com.notfound.payment.domain.model.DepositType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DepositRepositoryAdapter implements DepositPort {

    private final DepositJpaRepository depositJpaRepository;

    @Override
    public Deposit save(Deposit deposit) {
        return depositJpaRepository.save(DepositJpaEntity.fromModel(deposit)).toModel();
    }

    @Override
    public Optional<Deposit> findById(UUID id) {
        return depositJpaRepository.findById(id).map(DepositJpaEntity::toModel);
    }

    @Override
    public Page<Deposit> findByMemberId(UUID memberId, Pageable pageable) {
        return depositJpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(DepositJpaEntity::toModel);
    }

    @Override
    public Page<Deposit> findByMemberIdAndType(UUID memberId, DepositType type, Pageable pageable) {
        return depositJpaRepository.findByMemberIdAndTypeOrderByCreatedAtDesc(memberId, type, pageable)
                .map(DepositJpaEntity::toModel);
    }

    @Override
    public Optional<Deposit> findByPaymentId(UUID paymentId) {
        return depositJpaRepository.findByPaymentId(paymentId).map(DepositJpaEntity::toModel);
    }
}
