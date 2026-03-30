package com.notfound.order.infrastructure.persistence;

import com.notfound.order.application.port.out.CartItemRepository;
import com.notfound.order.domain.model.CartItem;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CartItemRepositoryAdapter implements CartItemRepository {

    private final CartItemJpaRepository cartItemJpaRepository;

    public CartItemRepositoryAdapter(CartItemJpaRepository cartItemJpaRepository) {
        this.cartItemJpaRepository = cartItemJpaRepository;
    }

    @Override
    public List<CartItem> findByCartId(UUID cartId) {
        return cartItemJpaRepository.findByCartId(cartId).stream()
                .map(CartItemJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<CartItem> findById(UUID cartItemId) {
        return cartItemJpaRepository.findById(cartItemId).map(CartItemJpaEntity::toDomain);
    }

    @Override
    public Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId) {
        return cartItemJpaRepository.findByCartIdAndProductId(cartId, productId)
                .map(CartItemJpaEntity::toDomain);
    }

    @Override
    public CartItem save(CartItem cartItem) {
        return cartItemJpaRepository.save(CartItemJpaEntity.from(cartItem)).toDomain();
    }

    @Override
    public void deleteById(UUID cartItemId) {
        cartItemJpaRepository.deleteById(cartItemId);
    }

    @Override
    public void deleteByCartId(UUID cartId) {
        cartItemJpaRepository.deleteByCartId(cartId);
    }
}
