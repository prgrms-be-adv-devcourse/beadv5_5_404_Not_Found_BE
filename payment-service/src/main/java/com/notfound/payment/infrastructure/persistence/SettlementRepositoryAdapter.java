package com.notfound.payment.infrastructure.persistence;

import com.notfound.payment.application.port.out.SettlementPort;
import com.notfound.payment.domain.model.Settlement;
import com.notfound.payment.domain.model.SettlementStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SettlementRepositoryAdapter implements SettlementPort {

    private final SettlementJpaRepository settlementJpaRepository;

    @Override
    public Settlement save(Settlement settlement) {
        return settlementJpaRepository.save(SettlementJpaEntity.fromModel(settlement)).toModel();
    }

    @Override
    public Optional<Settlement> findById(UUID id) {
        return settlementJpaRepository.findById(id).map(SettlementJpaEntity::toModel);
    }

    @Override
    public List<Settlement> findByPaymentId(UUID paymentId) {
        return settlementJpaRepository.findByPaymentId(paymentId).stream()
                .map(SettlementJpaEntity::toModel)
                .toList();
    }

    @Override
    public List<Settlement> findBySellerId(UUID sellerId) {
        return settlementJpaRepository.findBySellerId(sellerId).stream()
                .map(SettlementJpaEntity::toModel)
                .toList();
    }

    @Override
    public List<Settlement> findBySellerIdAndStatus(UUID sellerId, SettlementStatus status) {
        return settlementJpaRepository.findBySellerIdAndStatus(sellerId, status).stream()
                .map(SettlementJpaEntity::toModel)
                .toList();
    }

    @Override
    public List<Settlement> findBySettlementDate(LocalDate settlementDate) {
        return settlementJpaRepository.findBySettlementDate(settlementDate).stream()
                .map(SettlementJpaEntity::toModel)
                .toList();
    }
}
