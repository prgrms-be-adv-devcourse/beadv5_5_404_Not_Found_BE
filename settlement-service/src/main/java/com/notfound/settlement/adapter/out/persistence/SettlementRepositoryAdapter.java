package com.notfound.settlement.adapter.out.persistence;

import com.notfound.settlement.application.port.out.SettlementRepository;
import com.notfound.settlement.domain.model.Settlement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SettlementRepositoryAdapter implements SettlementRepository {

    private final SettlementJpaRepository settlementJpaRepository;

    @Override
    public Settlement save(Settlement settlement) {
        SettlementJpaEntity entity = settlementJpaRepository.findById(settlement.getId())
                .map(existing -> {
                    existing.updateFrom(settlement);
                    return existing;
                })
                .orElseGet(() -> SettlementJpaEntity.from(settlement));
        return settlementJpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Settlement> findById(UUID id) {
        return settlementJpaRepository.findById(id)
                .map(SettlementJpaEntity::toDomain);
    }

    @Override
    public List<Settlement> findBySellerId(UUID sellerId) {
        return settlementJpaRepository.findAllBySellerId(sellerId)
                .stream()
                .map(SettlementJpaEntity::toDomain)
                .toList();
    }
}
