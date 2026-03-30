package com.notfound.settlement.adapter.out.persistence;

import com.notfound.settlement.application.port.out.SettlementTargetRepository;
import com.notfound.settlement.domain.model.SettlementTarget;
import com.notfound.settlement.domain.model.SettlementTargetStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class SettlementTargetRepositoryAdapter implements SettlementTargetRepository {

    private final SettlementTargetJpaRepository settlementTargetJpaRepository;

    @Value("${settlement.batch.chunk-size}")
    private int chunkSize;

    @Override
    public SettlementTarget save(SettlementTarget settlementTarget) {
        SettlementTargetJpaEntity entity = settlementTargetJpaRepository.findById(settlementTarget.getId())
                .map(existing -> {
                    existing.updateFrom(settlementTarget);
                    return existing;
                })
                .orElseGet(() -> SettlementTargetJpaEntity.from(settlementTarget));
        return settlementTargetJpaRepository.save(entity).toDomain();
    }

    @Override
    public void saveAll(List<SettlementTarget> targets) {
        if (targets.isEmpty()) {
            return;
        }

        int totalChunks = (targets.size() + chunkSize - 1) / chunkSize;

        IntStream.range(0, totalChunks).forEach(i -> {
            List<SettlementTarget> chunk = targets.subList(
                    i * chunkSize,
                    Math.min((i + 1) * chunkSize, targets.size())
            );

            List<UUID> ids = chunk.stream().map(SettlementTarget::getId).toList();

            Map<UUID, SettlementTargetJpaEntity> existingMap = settlementTargetJpaRepository.findAllById(ids)
                    .stream()
                    .collect(Collectors.toMap(SettlementTargetJpaEntity::getId, e -> e));

            List<SettlementTargetJpaEntity> entities = chunk.stream()
                    .map(target -> {
                        SettlementTargetJpaEntity existing = existingMap.get(target.getId());
                        if (existing != null) {
                            existing.updateFrom(target);
                            return existing;
                        }
                        return SettlementTargetJpaEntity.from(target);
                    })
                    .toList();

            settlementTargetJpaRepository.saveAll(entities);
        });
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {
        return settlementTargetJpaRepository.existsByOrderId(orderId);
    }

    @Override
    public List<SettlementTarget> findPendingByConfirmedAtBetween(LocalDateTime from, LocalDateTime to) {
        return settlementTargetJpaRepository
                .findAllByStatusAndConfirmedAtBetween(SettlementTargetStatus.PENDING, from, to)
                .stream()
                .map(SettlementTargetJpaEntity::toDomain)
                .toList();
    }
}
