package com.notfound.settlement.application.service;

import com.notfound.settlement.application.port.in.ExecuteMonthlySettlementUseCase;
import com.notfound.settlement.application.port.in.GetSettlementHistoryUseCase;
import com.notfound.settlement.application.port.out.SellerAccountClient;
import com.notfound.settlement.application.port.out.SettlementRepository;
import com.notfound.settlement.application.port.out.SettlementTargetRepository;
import com.notfound.settlement.domain.event.SettlementCompletedEvent;
import com.notfound.settlement.domain.event.SettlementFailedEvent;
import com.notfound.settlement.domain.exception.SellerAccountNotFoundException;
import com.notfound.settlement.domain.model.Settlement;
import com.notfound.settlement.domain.model.SettlementStatus;
import com.notfound.settlement.domain.model.SettlementTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService implements ExecuteMonthlySettlementUseCase, GetSettlementHistoryUseCase {

    private final SettlementTargetRepository settlementTargetRepository;
    private final SettlementRepository settlementRepository;
    private final SellerAccountClient sellerAccountClient;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${settlement.fee-rate}")
    private double feeRate;

    @Transactional
    @Override
    public void execute(YearMonth targetMonth) {
        LocalDate periodStart = targetMonth.atDay(1);
        LocalDate periodEnd = targetMonth.atEndOfMonth();
        LocalDateTime from = periodStart.atStartOfDay();
        LocalDateTime to = periodEnd.atTime(LocalTime.MAX);

        List<SettlementTarget> pendingTargets =
                settlementTargetRepository.findPendingByConfirmedAtBetween(from, to);

        if (pendingTargets.isEmpty()) {
            log.info("[Settlement] No pending targets for period {} ~ {}", periodStart, periodEnd);
            return;
        }

        Map<UUID, List<SettlementTarget>> targetsBySeller = pendingTargets.stream()
                .collect(Collectors.groupingBy(SettlementTarget::getSellerId));

        for (Map.Entry<UUID, List<SettlementTarget>> entry : targetsBySeller.entrySet()) {
            UUID sellerId = entry.getKey();
            List<SettlementTarget> targets = entry.getValue();

            Optional<Settlement> existing = settlementRepository.findBySellerIdAndPeriod(sellerId, periodStart, periodEnd);
            if (existing.isPresent() && existing.get().getStatus() == SettlementStatus.COMPLETED) {
                log.info("[Settlement] Already COMPLETED for sellerId={}, period={} ~ {}", sellerId, periodStart, periodEnd);
                continue;
            }

            Settlement settlement = existing
                    .map(s -> { s.reset(); return s; })
                    .orElseGet(() -> Settlement.create(sellerId, periodStart, periodEnd, targets, feeRate));
            settlementRepository.save(settlement);

            try {
                sellerAccountClient.findSellerAccount(sellerId);

                settlement.complete();
                settlementRepository.save(settlement);

                targets.forEach(t -> t.settle(settlement.getId()));
                settlementTargetRepository.saveAll(targets);

                eventPublisher.publishEvent(new SettlementCompletedEvent(
                        settlement.getId(),
                        sellerId,
                        settlement.getNetAmount()
                ));

            } catch (SellerAccountNotFoundException e) {
                log.warn("[Settlement] Seller account not found for sellerId={}, period={} ~ {}",
                        sellerId, periodStart, periodEnd);

                settlement.fail();
                settlementRepository.save(settlement);

                eventPublisher.publishEvent(new SettlementFailedEvent(
                        sellerId,
                        periodStart,
                        periodEnd,
                        e.getMessage()
                ));
            }
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Settlement> getSettlements(UUID sellerId) {
        return settlementRepository.findBySellerId(sellerId);
    }
}
