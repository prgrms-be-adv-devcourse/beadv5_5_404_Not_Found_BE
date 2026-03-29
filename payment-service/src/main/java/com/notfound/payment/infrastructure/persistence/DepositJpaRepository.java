package com.notfound.payment.infrastructure.persistence;

import com.notfound.payment.domain.model.DepositType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DepositJpaRepository extends JpaRepository<DepositJpaEntity, UUID> {

    Page<DepositJpaEntity> findByMemberIdOrderByCreatedAtDesc(UUID memberId, Pageable pageable);

    Page<DepositJpaEntity> findByMemberIdAndTypeOrderByCreatedAtDesc(UUID memberId, DepositType type, Pageable pageable);

    Optional<DepositJpaEntity> findByPaymentId(UUID paymentId);
}
