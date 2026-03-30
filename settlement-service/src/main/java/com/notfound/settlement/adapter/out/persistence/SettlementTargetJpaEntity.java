package com.notfound.settlement.adapter.out.persistence;

import com.notfound.settlement.domain.model.SettlementTarget;
import com.notfound.settlement.domain.model.SettlementTargetStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlement_target")
@Getter
@NoArgsConstructor
public class SettlementTargetJpaEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "total_amount", nullable = false)
    private long totalAmount;

    @Column(name = "confirmed_at", nullable = false)
    private LocalDateTime confirmedAt;

    @Column(name = "settlement_id")
    private UUID settlementId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementTargetStatus status;

    public static SettlementTargetJpaEntity from(SettlementTarget settlementTarget) {
        SettlementTargetJpaEntity entity = new SettlementTargetJpaEntity();
        entity.id = settlementTarget.getId();
        entity.orderId = settlementTarget.getOrderId();
        entity.sellerId = settlementTarget.getSellerId();
        entity.totalAmount = settlementTarget.getTotalAmount();
        entity.confirmedAt = settlementTarget.getConfirmedAt();
        entity.settlementId = settlementTarget.getSettlementId();
        entity.status = settlementTarget.getStatus();
        return entity;
    }

    public void updateFrom(SettlementTarget settlementTarget) {
        this.settlementId = settlementTarget.getSettlementId();
        this.status = settlementTarget.getStatus();
    }

    public SettlementTarget toDomain() {
        return SettlementTarget.of(id, orderId, sellerId, totalAmount, confirmedAt, settlementId, status);
    }
}
