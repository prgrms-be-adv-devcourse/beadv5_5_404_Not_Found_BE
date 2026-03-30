package com.notfound.settlement.domain.model;

import com.notfound.settlement.domain.exception.InvalidSettlementStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettlementTest {

    private UUID sellerId;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
        periodStart = LocalDate.of(2025, 2, 1);
        periodEnd = LocalDate.of(2025, 2, 28);
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("생성 시 상태는 PENDING이다")
        void create_initialStatusIsPending() {
            List<SettlementTarget> targets = List.of(
                    SettlementTarget.create(UUID.randomUUID(), sellerId, 10000L, LocalDateTime.now())
            );

            Settlement settlement = Settlement.create(sellerId, periodStart, periodEnd, targets, 0.03);

            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.PENDING);
        }

        @Test
        @DisplayName("수수료는 총 매출의 feeRate를 반올림한 값이다")
        void create_feeCalculation() {
            List<SettlementTarget> targets = List.of(
                    SettlementTarget.create(UUID.randomUUID(), sellerId, 10000L, LocalDateTime.now()),
                    SettlementTarget.create(UUID.randomUUID(), sellerId, 20000L, LocalDateTime.now())
            );

            Settlement settlement = Settlement.create(sellerId, periodStart, periodEnd, targets, 0.03);

            assertThat(settlement.getTotalSalesAmount()).isEqualTo(30000L);
            assertThat(settlement.getFeeAmount()).isEqualTo(Math.round(30000L * 0.03));
            assertThat(settlement.getNetAmount()).isEqualTo(30000L - Math.round(30000L * 0.03));
        }

        @Test
        @DisplayName("여러 타겟의 totalAmount를 합산한다")
        void create_aggregatesTotalAmount() {
            List<SettlementTarget> targets = List.of(
                    SettlementTarget.create(UUID.randomUUID(), sellerId, 5000L, LocalDateTime.now()),
                    SettlementTarget.create(UUID.randomUUID(), sellerId, 7000L, LocalDateTime.now()),
                    SettlementTarget.create(UUID.randomUUID(), sellerId, 3000L, LocalDateTime.now())
            );

            Settlement settlement = Settlement.create(sellerId, periodStart, periodEnd, targets, 0.03);

            assertThat(settlement.getTotalSalesAmount()).isEqualTo(15000L);
        }
    }

    @Nested
    @DisplayName("complete()")
    class Complete {

        @Test
        @DisplayName("PENDING 상태에서 complete() 호출 시 COMPLETED로 전이된다")
        void complete_success() {
            Settlement settlement = Settlement.create(sellerId, periodStart, periodEnd,
                    List.of(SettlementTarget.create(UUID.randomUUID(), sellerId, 10000L, LocalDateTime.now())),
                    0.03);

            settlement.complete();

            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
        }

        @Test
        @DisplayName("COMPLETED 상태에서 complete() 호출 시 예외가 발생한다")
        void complete_alreadyCompleted_throws() {
            Settlement settlement = Settlement.of(
                    UUID.randomUUID(), sellerId, periodStart, periodEnd,
                    10000L, 300L, 9700L, LocalDateTime.now(), SettlementStatus.COMPLETED);

            assertThatThrownBy(settlement::complete)
                    .isInstanceOf(InvalidSettlementStatusException.class);
        }
    }

    @Nested
    @DisplayName("fail()")
    class Fail {

        @Test
        @DisplayName("PENDING 상태에서 fail() 호출 시 FAILED로 전이된다")
        void fail_success() {
            Settlement settlement = Settlement.create(sellerId, periodStart, periodEnd,
                    List.of(SettlementTarget.create(UUID.randomUUID(), sellerId, 10000L, LocalDateTime.now())),
                    0.03);

            settlement.fail();

            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.FAILED);
        }

        @Test
        @DisplayName("FAILED 상태에서 fail() 호출 시 예외가 발생한다")
        void fail_alreadyFailed_throws() {
            Settlement settlement = Settlement.of(
                    UUID.randomUUID(), sellerId, periodStart, periodEnd,
                    10000L, 300L, 9700L, LocalDateTime.now(), SettlementStatus.FAILED);

            assertThatThrownBy(settlement::fail)
                    .isInstanceOf(InvalidSettlementStatusException.class);
        }
    }
}
