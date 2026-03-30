package com.notfound.order.application.service;

import com.notfound.order.application.port.in.*;
import com.notfound.order.application.port.out.CartItemRepository;
import com.notfound.order.application.port.out.CartRepository;
import com.notfound.order.domain.exception.OrderException;
import com.notfound.order.domain.model.Cart;
import com.notfound.order.domain.model.CartItem;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CartService implements AddCartItemUseCase, GetCartUseCase,
        UpdateCartItemUseCase, DeleteCartItemUseCase, ClearCartUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    @Transactional
    public CartItem addCartItem(UUID memberId, UUID productId, int quantity) {
        Cart cart = getOrCreateCart(memberId);

        return cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .map(existing -> {
                    existing.updateQuantity(existing.getQuantity() + quantity);
                    return cartItemRepository.save(existing);
                })
                .orElseGet(() -> {
                    try {
                        return cartItemRepository.save(
                                CartItem.builder()
                                        .cartId(cart.getId())
                                        .productId(productId)
                                        .quantity(quantity)
                                        .build());
                    } catch (DataIntegrityViolationException e) {
                        // 동시 요청으로 중복 생성된 경우 → 재조회 후 수량 합산
                        CartItem existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                                .orElseThrow(OrderException::cartItemNotFound);
                        existing.updateQuantity(existing.getQuantity() + quantity);
                        return cartItemRepository.save(existing);
                    }
                });
    }

    private Cart getOrCreateCart(UUID memberId) {
        return cartRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    try {
                        return cartRepository.save(Cart.builder().memberId(memberId).build());
                    } catch (DataIntegrityViolationException e) {
                        // 동시 최초 요청으로 중복 생성된 경우 → 재조회
                        return cartRepository.findByMemberId(memberId)
                                .orElseThrow(OrderException::cartNotFound);
                    }
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getCart(UUID memberId) {
        return cartRepository.findByMemberId(memberId)
                .orElseThrow(OrderException::cartNotFound);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(UUID memberId) {
        Cart cart = getCart(memberId);
        return cartItemRepository.findByCartId(cart.getId());
    }

    @Override
    @Transactional
    public CartItem updateCartItemQuantity(UUID memberId, UUID cartItemId, int quantity) {
        Cart cart = getCart(memberId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(OrderException::cartItemNotFound);

        if (!item.getCartId().equals(cart.getId())) {
            throw OrderException.cartItemAccessDenied();
        }

        item.updateQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Override
    @Transactional
    public void deleteCartItem(UUID memberId, UUID cartItemId) {
        Cart cart = getCart(memberId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(OrderException::cartItemNotFound);

        if (!item.getCartId().equals(cart.getId())) {
            throw OrderException.cartItemAccessDenied();
        }

        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(UUID memberId) {
        Cart cart = getCart(memberId);
        cartItemRepository.deleteByCartId(cart.getId());
    }
}
