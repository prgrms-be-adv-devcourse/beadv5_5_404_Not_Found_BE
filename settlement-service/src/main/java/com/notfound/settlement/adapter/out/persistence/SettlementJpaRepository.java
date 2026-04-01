package com.notfound.settlement.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SettlementJpaRepository extends JpaRepository<SettlementJpaEntity, UUID> {

    List<SettlementJpaEntity> findAllBySellerId(UUID sellerId);
}
