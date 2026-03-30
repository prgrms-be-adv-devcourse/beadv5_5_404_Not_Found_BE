package com.notfound.order.infrastructure.persistence;

import com.notfound.order.application.port.out.CartRepository;
import com.notfound.order.domain.model.Cart;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CartRepositoryAdapter implements CartRepository {

    private final CartJpaRepository cartJpaRepository;

    public CartRepositoryAdapter(CartJpaRepository cartJpaRepository) {
        this.cartJpaRepository = cartJpaRepository;
    }

    @Override
    public Optional<Cart> findByMemberId(UUID memberId) {
        return cartJpaRepository.findByMemberId(memberId).map(CartJpaEntity::toDomain);
    }

    @Override
    public Cart save(Cart cart) {
        return cartJpaRepository.save(CartJpaEntity.from(cart)).toDomain();
    }
}
