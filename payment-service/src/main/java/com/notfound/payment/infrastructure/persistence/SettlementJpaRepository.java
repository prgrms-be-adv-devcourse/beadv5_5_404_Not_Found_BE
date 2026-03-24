package com.notfound.payment.infrastructure.persistence;

import com.notfound.payment.domain.model.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SettlementJpaRepository extends JpaRepository<SettlementJpaEntity, UUID> {

    List<SettlementJpaEntity> findByPaymentId(UUID paymentId);

    List<SettlementJpaEntity> findBySellerId(UUID sellerId);

    List<SettlementJpaEntity> findBySellerIdAndStatus(UUID sellerId, SettlementStatus status);

    List<SettlementJpaEntity> findBySettlementDate(LocalDate settlementDate);
}
