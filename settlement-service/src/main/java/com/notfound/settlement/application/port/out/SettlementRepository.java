package com.notfound.settlement.application.port.out;

import com.notfound.settlement.domain.model.Settlement;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepository {

    Settlement save(Settlement settlement);

    Optional<Settlement> findById(UUID id);

    List<Settlement> findBySellerId(UUID sellerId);

    Optional<Settlement> findBySellerIdAndPeriod(UUID sellerId, LocalDate periodStart, LocalDate periodEnd);
}
