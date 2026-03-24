package com.notfound.payment.infrastructure.persistence;

import com.notfound.payment.domain.model.DepositType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepositJpaRepository extends JpaRepository<DepositJpaEntity, UUID> {

    List<DepositJpaEntity> findByMemberIdOrderByCreatedAtDesc(UUID memberId);

    List<DepositJpaEntity> findByMemberIdAndTypeOrderByCreatedAtDesc(UUID memberId, DepositType type);

    Optional<DepositJpaEntity> findByPaymentId(UUID paymentId);
}
