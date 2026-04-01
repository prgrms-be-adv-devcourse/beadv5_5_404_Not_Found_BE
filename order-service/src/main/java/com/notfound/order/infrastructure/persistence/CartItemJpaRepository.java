package com.notfound.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemJpaRepository extends JpaRepository<CartItemJpaEntity, UUID> {
    List<CartItemJpaEntity> findByCartId(UUID cartId);
    Optional<CartItemJpaEntity> findByCartIdAndProductId(UUID cartId, UUID productId);
    void deleteByCartId(UUID cartId);
}
