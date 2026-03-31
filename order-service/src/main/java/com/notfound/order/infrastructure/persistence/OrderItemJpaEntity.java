package com.notfound.order.infrastructure.persistence;

import com.notfound.order.domain.model.OrderItem;
import com.notfound.order.domain.model.OrderItemStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private UUID sellerId;

    @Column(nullable = false, length = 300)
    private String productTitle;

    @Column(nullable = false)
    private int unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int subtotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderItemStatus status;

    public static OrderItemJpaEntity from(OrderItem item) {
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.id = item.getId();
        entity.orderId = item.getOrderId();
        entity.productId = item.getProductId();
        entity.sellerId = item.getSellerId();
        entity.productTitle = item.getProductTitle();
        entity.unitPrice = item.getUnitPrice();
        entity.quantity = item.getQuantity();
        entity.subtotal = item.getSubtotal();
        entity.status = item.getStatus();
        return entity;
    }

    public OrderItem toDomain() {
        return OrderItem.builder()
                .id(id)
                .orderId(orderId)
                .productId(productId)
                .sellerId(sellerId)
                .productTitle(productTitle)
                .unitPrice(unitPrice)
                .quantity(quantity)
                .subtotal(subtotal)
                .status(status)
                .build();
    }
}
