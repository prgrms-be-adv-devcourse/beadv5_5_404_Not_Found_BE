package com.notfound.settlement.adapter.in.scheduler;

import com.notfound.settlement.application.port.in.ExecuteMonthlySettlementUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final ExecuteMonthlySettlementUseCase executeMonthlySettlementUseCase;

    @Scheduled(cron = "0 0 0 25 * *")
    @SchedulerLock(name = "monthly-settlement", lockAtMostFor = "PT1H", lockAtLeastFor = "PT10M")
    public void executeMonthlySettlement() {
        YearMonth targetMonth = YearMonth.now().minusMonths(1);
        log.info("[SettlementScheduler] 월 정산 실행 시작 — targetMonth={}", targetMonth);
        executeMonthlySettlementUseCase.execute(targetMonth);
        log.info("[SettlementScheduler] 월 정산 실행 완료 — targetMonth={}", targetMonth);
    }
}
