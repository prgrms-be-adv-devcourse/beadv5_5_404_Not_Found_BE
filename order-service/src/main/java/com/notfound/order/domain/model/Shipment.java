package com.notfound.order.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Shipment {

    private UUID id;
    private UUID orderId;
    private String carrier;
    private String trackingNumber;
    private ShipmentStatus status;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    private Shipment() {}

    public static Builder builder() { return new Builder(); }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public String getCarrier() { return carrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public ShipmentStatus getStatus() { return status; }
    public LocalDateTime getShippedAt() { return shippedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }

    public void update(String carrier, String trackingNumber, ShipmentStatus status) {
        if (carrier != null) this.carrier = carrier;
        if (trackingNumber != null) this.trackingNumber = trackingNumber;
        if (status != null) {
            this.status = status;
            if (status == ShipmentStatus.SHIPPED && this.shippedAt == null) {
                this.shippedAt = LocalDateTime.now();
            }
            if (status == ShipmentStatus.DELIVERED) {
                this.deliveredAt = LocalDateTime.now();
            }
        }
    }

    public static class Builder {
        private final Shipment s = new Shipment();
        public Builder id(UUID id) { s.id = id; return this; }
        public Builder orderId(UUID orderId) { s.orderId = orderId; return this; }
        public Builder carrier(String carrier) { s.carrier = carrier; return this; }
        public Builder trackingNumber(String trackingNumber) { s.trackingNumber = trackingNumber; return this; }
        public Builder status(ShipmentStatus status) { s.status = status; return this; }
        public Builder shippedAt(LocalDateTime shippedAt) { s.shippedAt = shippedAt; return this; }
        public Builder deliveredAt(LocalDateTime deliveredAt) { s.deliveredAt = deliveredAt; return this; }
        public Shipment build() { return s; }
    }
}
