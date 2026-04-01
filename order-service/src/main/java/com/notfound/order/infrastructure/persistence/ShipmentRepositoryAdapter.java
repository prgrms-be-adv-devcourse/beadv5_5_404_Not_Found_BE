package com.notfound.order.infrastructure.persistence;

import com.notfound.order.application.port.out.ShipmentRepository;
import com.notfound.order.domain.model.Shipment;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ShipmentRepositoryAdapter implements ShipmentRepository {

    private final ShipmentJpaRepository shipmentJpaRepository;

    public ShipmentRepositoryAdapter(ShipmentJpaRepository shipmentJpaRepository) {
        this.shipmentJpaRepository = shipmentJpaRepository;
    }

    @Override
    public Shipment save(Shipment shipment) {
        return shipmentJpaRepository.save(ShipmentJpaEntity.from(shipment)).toDomain();
    }

    @Override
    public Optional<Shipment> findByOrderId(UUID orderId) {
        return shipmentJpaRepository.findByOrderId(orderId).map(ShipmentJpaEntity::toDomain);
    }
}
