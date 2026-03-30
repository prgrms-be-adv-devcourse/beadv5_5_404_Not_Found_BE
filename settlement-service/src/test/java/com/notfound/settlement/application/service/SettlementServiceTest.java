package com.notfound.settlement.application.service;

import com.notfound.settlement.application.port.out.SellerAccountClient;
import com.notfound.settlement.application.port.out.SellerAccount;
import com.notfound.settlement.application.port.out.SettlementRepository;
import com.notfound.settlement.application.port.out.SettlementTargetRepository;
import com.notfound.settlement.domain.event.SettlementCompletedEvent;
import com.notfound.settlement.domain.event.SettlementFailedEvent;
import com.notfound.settlement.domain.exception.SellerAccountNotFoundException;
import com.notfound.settlement.domain.model.Settlement;
import com.notfound.settlement.domain.model.SettlementStatus;
import com.notfound.settlement.domain.model.SettlementTarget;
import com.notfound.settlement.domain.model.SettlementTargetStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private SettlementTargetRepository settlementTargetRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private SellerAccountClient sellerAccountClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SettlementService settlementService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(settlementService, "feeRate", 0.03);
    }

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("PENDING 타겟이 없으면 아무 처리도 하지 않는다")
        void noPendingTargets_doNothing() {
            given(settlementTargetRepository.findPendingByConfirmedAtBetween(any(), any()))
                    .willReturn(List.of());

            settlementService.execute(YearMonth.of(2025, 2));

            verify(settlementRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("계좌 조회 성공 시 Settlement가 COMPLETED 상태로 저장되고 SettlementCompletedEvent가 발행된다")
        void accountFound_completedAndEventPublished() {
            UUID sellerId = UUID.randomUUID();
            List<SettlementTarget> targets = List.of(
                    SettlementTarget.create(UUID.randomUUID(), sellerId, 10000L, LocalDateTime.now())
            );
            given(settlementTargetRepository.findPendingByConfirmedAtBetween(any(), any()))
                    .willReturn(targets);
            given(sellerAccountClient.findSellerAccount(sellerId))
                    .willReturn(new SellerAccount("004", "123-456", "홍길동"));

            settlementService.execute(YearMonth.of(2025, 2));

            ArgumentCaptor<Settlement> settlementCaptor = ArgumentCaptor.forClass(Settlement.class);
            verify(settlementRepository, times(2)).save(settlementCaptor.capture());
            assertThat(settlementCaptor.getValue().getStatus()).isEqualTo(SettlementStatus.COMPLETED);

            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue()).isInstanceOf(SettlementCompletedEvent.class);
        }

        @Test
        @DisplayName("계좌 조회 실패 시 Settlement가 FAILED 상태로 저장되고 SettlementFailedEvent가 발행된다")
        void accountNotFound_failedAndEventPublished() {
            UUID sellerId = UUID.randomUUID();
            List<SettlementTarget> targets = List.of(
                    SettlementTarget.create(UUID.randomUUID(), sellerId, 10000L, LocalDateTime.now())
            );
            given(settlementTargetRepository.findPendingByConfirmedAtBetween(any(), any()))
                    .willReturn(targets);
            willThrow(new SellerAccountNotFoundException(sellerId))
                    .given(sellerAccountClient).findSellerAccount(sellerId);

            settlementService.execute(YearMonth.of(2025, 2));

            ArgumentCaptor<Settlement> settlementCaptor = ArgumentCaptor.forClass(Settlement.class);
            verify(settlementRepository, times(2)).save(settlementCaptor.capture());
            assertThat(settlementCaptor.getValue().getStatus()).isEqualTo(SettlementStatus.FAILED);

            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue()).isInstanceOf(SettlementFailedEvent.class);
        }

        @Test
        @DisplayName("계좌 조회 성공 시 타겟은 SETTLED 상태로 변경된다")
        void accountFound_targetSettled() {
            UUID sellerId = UUID.randomUUID();
            SettlementTarget target = SettlementTarget.create(
                    UUID.randomUUID(), sellerId, 10000L, LocalDateTime.now());
            given(settlementTargetRepository.findPendingByConfirmedAtBetween(any(), any()))
                    .willReturn(List.of(target));
            given(sellerAccountClient.findSellerAccount(sellerId))
                    .willReturn(new SellerAccount("004", "123-456", "홍길동"));

            settlementService.execute(YearMonth.of(2025, 2));

            assertThat(target.getStatus()).isEqualTo(SettlementTargetStatus.SETTLED);
            verify(settlementTargetRepository).saveAll(List.of(target));
        }

        @Test
        @DisplayName("계좌 조회 실패 시 타겟은 PENDING 상태를 유지한다")
        void accountNotFound_targetRemainingPending() {
            UUID sellerId = UUID.randomUUID();
            SettlementTarget target = SettlementTarget.create(
                    UUID.randomUUID(), sellerId, 10000L, LocalDateTime.now());
            given(settlementTargetRepository.findPendingByConfirmedAtBetween(any(), any()))
                    .willReturn(List.of(target));
            willThrow(new SellerAccountNotFoundException(sellerId))
                    .given(sellerAccountClient).findSellerAccount(sellerId);

            settlementService.execute(YearMonth.of(2025, 2));

            assertThat(target.getStatus()).isEqualTo(SettlementTargetStatus.PENDING);
            verify(settlementTargetRepository, never()).saveAll(any());
        }
    }
}
