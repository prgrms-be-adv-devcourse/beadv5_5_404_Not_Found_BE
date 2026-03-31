package com.notfound.order.presentation.dto;

import com.notfound.order.domain.model.Shipment;

import java.util.UUID;

public record ShipmentResponse(
        UUID orderId,
        String carrier,
        String trackingNumber,
        String shipmentStatus
) {
    public static ShipmentResponse from(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getOrderId(),
                shipment.getCarrier(),
                shipment.getTrackingNumber(),
                shipment.getStatus().name());
    }
}
