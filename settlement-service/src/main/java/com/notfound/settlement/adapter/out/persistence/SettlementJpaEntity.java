package com.notfound.settlement.adapter.out.persistence;

import com.notfound.settlement.domain.model.Settlement;
import com.notfound.settlement.domain.model.SettlementStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlement")
@Getter
@NoArgsConstructor
public class SettlementJpaEntity {

    @Id
    private UUID id;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "total_sales_amount", nullable = false)
    private long totalSalesAmount;

    @Column(name = "fee_amount", nullable = false)
    private long feeAmount;

    @Column(name = "net_amount", nullable = false)
    private long netAmount;

    @Column(name = "settled_at", nullable = false)
    private LocalDateTime settledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    public static SettlementJpaEntity from(Settlement settlement) {
        SettlementJpaEntity entity = new SettlementJpaEntity();
        entity.id = settlement.getId();
        entity.sellerId = settlement.getSellerId();
        entity.periodStart = settlement.getPeriodStart();
        entity.periodEnd = settlement.getPeriodEnd();
        entity.totalSalesAmount = settlement.getTotalSalesAmount();
        entity.feeAmount = settlement.getFeeAmount();
        entity.netAmount = settlement.getNetAmount();
        entity.settledAt = settlement.getSettledAt();
        entity.status = settlement.getStatus();
        return entity;
    }

    public void updateFrom(Settlement settlement) {
        this.status = settlement.getStatus();
    }

    public Settlement toDomain() {
        return Settlement.of(id, sellerId, periodStart, periodEnd,
                totalSalesAmount, feeAmount, netAmount, settledAt, status);
    }
}
