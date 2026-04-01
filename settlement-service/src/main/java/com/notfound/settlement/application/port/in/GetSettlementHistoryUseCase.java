package com.notfound.settlement.application.port.in;

import com.notfound.settlement.domain.model.Settlement;

import java.util.List;
import java.util.UUID;

public interface GetSettlementHistoryUseCase {

    List<Settlement> getSettlements(UUID sellerId);
}
