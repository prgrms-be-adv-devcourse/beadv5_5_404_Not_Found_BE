package com.notfound.payment.infrastructure.persistence;

import com.notfound.payment.application.port.out.DepositPort;
import com.notfound.payment.domain.model.Deposit;
import com.notfound.payment.domain.model.DepositType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public List<Deposit> findByMemberId(UUID memberId) {
        return depositJpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(DepositJpaEntity::toModel)
                .toList();
    }

    @Override
    public List<Deposit> findByMemberIdAndType(UUID memberId, DepositType type) {
        return depositJpaRepository.findByMemberIdAndTypeOrderByCreatedAtDesc(memberId, type).stream()
                .map(DepositJpaEntity::toModel)
                .toList();
    }

    @Override
    public Optional<Deposit> findByPaymentId(UUID paymentId) {
        return depositJpaRepository.findByPaymentId(paymentId).map(DepositJpaEntity::toModel);
    }
}
