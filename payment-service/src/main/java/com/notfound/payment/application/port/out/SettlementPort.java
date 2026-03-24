package com.notfound.payment.application.port.out;

import com.notfound.payment.domain.model.Settlement;
import com.notfound.payment.domain.model.SettlementStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementPort {

    Settlement save(Settlement settlement);

    Optional<Settlement> findById(UUID id);

    List<Settlement> findByPaymentId(UUID paymentId);

    List<Settlement> findBySellerId(UUID sellerId);

    List<Settlement> findBySellerIdAndStatus(UUID sellerId, SettlementStatus status);

    List<Settlement> findBySettlementDate(LocalDate settlementDate);
}
