package com.notfound.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CartJpaRepository extends JpaRepository<CartJpaEntity, UUID> {
    Optional<CartJpaEntity> findByMemberId(UUID memberId);
}
