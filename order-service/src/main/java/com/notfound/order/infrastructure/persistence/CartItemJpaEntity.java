package com.notfound.order.infrastructure.persistence;

import com.notfound.order.domain.model.CartItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cart_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID cartId;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private int quantity;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static CartItemJpaEntity from(CartItem item) {
        CartItemJpaEntity entity = new CartItemJpaEntity();
        entity.id = item.getId();
        entity.cartId = item.getCartId();
        entity.productId = item.getProductId();
        entity.quantity = item.getQuantity();
        entity.createdAt = item.getCreatedAt();
        return entity;
    }

    public CartItem toDomain() {
        return CartItem.builder()
                .id(id)
                .cartId(cartId)
                .productId(productId)
                .quantity(quantity)
                .createdAt(createdAt)
                .build();
    }
}
