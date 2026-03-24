package com.notfound.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RefundJpaRepository extends JpaRepository<RefundJpaEntity, UUID> {

    List<RefundJpaEntity> findByPaymentId(UUID paymentId);

    List<RefundJpaEntity> findByOrderItemId(UUID orderItemId);
}
