package com.notfound.order.infrastructure.persistence;

import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Column(nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private int totalAmount;

    @Column(nullable = false)
    private int shippingFee;

    @Column(nullable = false)
    private int depositUsed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String shippingSnapshot;

    @Column(nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static OrderJpaEntity from(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.id = order.getId();
        entity.orderNumber = order.getOrderNumber();
        entity.memberId = order.getMemberId();
        entity.status = order.getStatus();
        entity.totalAmount = order.getTotalAmount();
        entity.shippingFee = order.getShippingFee();
        entity.depositUsed = order.getDepositUsed();
        entity.shippingSnapshot = order.getShippingSnapshot();
        entity.idempotencyKey = order.getIdempotencyKey();
        entity.createdAt = order.getCreatedAt();
        return entity;
    }

    public Order toDomain() {
        return Order.builder()
                .id(id)
                .orderNumber(orderNumber)
                .memberId(memberId)
                .status(status)
                .totalAmount(totalAmount)
                .shippingFee(shippingFee)
                .depositUsed(depositUsed)
                .shippingSnapshot(shippingSnapshot)
                .idempotencyKey(idempotencyKey)
                .createdAt(createdAt)
                .build();
    }
}
