package com.notfound.payment.infrastructure.persistence;

import com.notfound.payment.domain.model.Settlement;
import com.notfound.payment.domain.model.SettlementStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementJpaEntity {

    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "seller_id", columnDefinition = "uuid", nullable = false)
    private UUID sellerId;

    @Column(name = "order_item_id", columnDefinition = "uuid", nullable = false)
    private UUID orderItemId;

    @Column(name = "payment_id", columnDefinition = "uuid", nullable = false)
    private UUID paymentId;

    @Column(name = "gross_amount", nullable = false)
    private int grossAmount;

    @Column(name = "commission_amount", nullable = false)
    private int commissionAmount;

    @Column(name = "net_amount", nullable = false)
    private int netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    public static SettlementJpaEntity fromModel(Settlement settlement) {
        SettlementJpaEntity entity = new SettlementJpaEntity();
        entity.id = settlement.getId();
        entity.sellerId = settlement.getSellerId();
        entity.orderItemId = settlement.getOrderItemId();
        entity.paymentId = settlement.getPaymentId();
        entity.grossAmount = settlement.getGrossAmount();
        entity.commissionAmount = settlement.getCommissionAmount();
        entity.netAmount = settlement.getNetAmount();
        entity.status = settlement.getStatus();
        entity.settlementDate = settlement.getSettlementDate();
        entity.settledAt = settlement.getSettledAt();
        return entity;
    }

    public Settlement toModel() {
        return Settlement.of(
                id,
                sellerId,
                orderItemId,
                paymentId,
                grossAmount,
                commissionAmount,
                netAmount,
                status,
                settlementDate,
                settledAt
        );
    }
}
