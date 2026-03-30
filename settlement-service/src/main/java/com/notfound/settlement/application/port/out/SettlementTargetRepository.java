package com.notfound.settlement.application.port.out;

import com.notfound.settlement.domain.model.SettlementTarget;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SettlementTargetRepository {

    SettlementTarget save(SettlementTarget settlementTarget);

    void saveAll(List<SettlementTarget> settlementTargets);

    boolean existsByOrderId(UUID orderId);

    List<SettlementTarget> findPendingByConfirmedAtBetween(LocalDateTime from, LocalDateTime to);
}
