package com.notfound.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentJpaRepository extends JpaRepository<ShipmentJpaEntity, UUID> {
    Optional<ShipmentJpaEntity> findByOrderId(UUID orderId);
}
