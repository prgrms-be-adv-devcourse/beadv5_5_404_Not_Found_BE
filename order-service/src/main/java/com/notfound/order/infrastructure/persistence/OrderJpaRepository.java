package com.notfound.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {
    Optional<OrderJpaEntity> findByIdempotencyKey(String idempotencyKey);
    boolean existsByIdempotencyKey(String idempotencyKey);
}
