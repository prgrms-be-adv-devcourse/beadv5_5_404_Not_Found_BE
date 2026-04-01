package com.notfound.settlement.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementJpaRepository extends JpaRepository<SettlementJpaEntity, UUID> {

    List<SettlementJpaEntity> findAllBySellerId(UUID sellerId);

    Optional<SettlementJpaEntity> findBySellerIdAndPeriodStartAndPeriodEnd(UUID sellerId, LocalDate periodStart, LocalDate periodEnd);
}
