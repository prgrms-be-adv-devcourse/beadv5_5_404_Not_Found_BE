package com.notfound.order.infrastructure.persistence;

import com.notfound.order.domain.model.Shipment;
import com.notfound.order.domain.model.ShipmentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShipmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID orderId;

    @Column(length = 50)
    private String carrier;

    @Column(length = 50)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    private LocalDateTime shippedAt;

    private LocalDateTime deliveredAt;

    public static ShipmentJpaEntity from(Shipment shipment) {
        ShipmentJpaEntity entity = new ShipmentJpaEntity();
        entity.id = shipment.getId();
        entity.orderId = shipment.getOrderId();
        entity.carrier = shipment.getCarrier();
        entity.trackingNumber = shipment.getTrackingNumber();
        entity.status = shipment.getStatus();
        entity.shippedAt = shipment.getShippedAt();
        entity.deliveredAt = shipment.getDeliveredAt();
        return entity;
    }

    public Shipment toDomain() {
        return Shipment.builder()
                .id(id)
                .orderId(orderId)
                .carrier(carrier)
                .trackingNumber(trackingNumber)
                .status(status)
                .shippedAt(shippedAt)
                .deliveredAt(deliveredAt)
                .build();
    }
}
