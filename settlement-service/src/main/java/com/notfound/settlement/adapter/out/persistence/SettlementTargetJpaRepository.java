package com.notfound.settlement.adapter.out.persistence;

import com.notfound.settlement.domain.model.SettlementTargetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SettlementTargetJpaRepository extends JpaRepository<SettlementTargetJpaEntity, UUID> {

    boolean existsByOrderId(UUID orderId);

    List<SettlementTargetJpaEntity> findAllByStatusAndConfirmedAtBetween(
            SettlementTargetStatus status,
            LocalDateTime from,
            LocalDateTime to
    );
}
