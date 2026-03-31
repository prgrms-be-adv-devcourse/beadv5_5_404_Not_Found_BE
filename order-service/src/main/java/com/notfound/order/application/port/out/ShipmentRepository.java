package com.notfound.order.application.port.out;

import com.notfound.order.domain.model.Shipment;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository {
    Shipment save(Shipment shipment);
    Optional<Shipment> findByOrderId(UUID orderId);
}
