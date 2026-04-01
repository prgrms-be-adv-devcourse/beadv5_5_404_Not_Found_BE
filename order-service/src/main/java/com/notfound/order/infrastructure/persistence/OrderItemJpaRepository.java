package com.notfound.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemJpaEntity, UUID> {
    List<OrderItemJpaEntity> findByOrderId(UUID orderId);
}
