package com.notfound.order.application.port.in;

import com.notfound.order.domain.model.Shipment;
import com.notfound.order.domain.model.ShipmentStatus;

import java.util.UUID;

public interface UpdateShipmentUseCase {
    Shipment updateShipment(UUID memberId, UUID orderId, String carrier, String trackingNumber, ShipmentStatus status);
}
