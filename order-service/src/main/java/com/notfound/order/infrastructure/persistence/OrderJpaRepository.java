package com.notfound.order.infrastructure.persistence;

import com.notfound.order.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {
    Optional<OrderJpaEntity> findByIdempotencyKey(String idempotencyKey);
    boolean existsByIdempotencyKey(String idempotencyKey);
    Page<OrderJpaEntity> findByMemberId(UUID memberId, Pageable pageable);
    Page<OrderJpaEntity> findByMemberIdAndStatus(UUID memberId, OrderStatus status, Pageable pageable);
    List<OrderJpaEntity> findByStatusAndDeliveredAtBefore(OrderStatus status, LocalDateTime before);
    List<OrderJpaEntity> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime before);
}
