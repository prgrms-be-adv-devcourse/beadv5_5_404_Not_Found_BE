package com.notfound.order.infrastructure.persistence;

import com.notfound.order.domain.model.Cart;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cart")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID memberId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static CartJpaEntity from(Cart cart) {
        CartJpaEntity entity = new CartJpaEntity();
        entity.id = cart.getId();
        entity.memberId = cart.getMemberId();
        entity.createdAt = cart.getCreatedAt();
        return entity;
    }

    public Cart toDomain() {
        return Cart.builder()
                .id(id)
                .memberId(memberId)
                .createdAt(createdAt)
                .build();
    }
}
