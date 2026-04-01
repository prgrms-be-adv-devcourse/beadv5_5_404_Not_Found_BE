package com.notfound.settlement.domain.model;

import com.notfound.settlement.domain.exception.InvalidSettlementTargetStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettlementTargetTest {

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("생성 시 상태는 PENDING, settlementId는 null이다")
        void create_initialState() {
            UUID orderId = UUID.randomUUID();
            UUID sellerId = UUID.randomUUID();
            LocalDateTime confirmedAt = LocalDateTime.now();

            SettlementTarget target = SettlementTarget.create(orderId, sellerId, 10000L, confirmedAt);

            assertThat(target.getStatus()).isEqualTo(SettlementTargetStatus.PENDING);
            assertThat(target.getSettlementId()).isNull();
            assertThat(target.getOrderId()).isEqualTo(orderId);
            assertThat(target.getSellerId()).isEqualTo(sellerId);
            assertThat(target.getTotalAmount()).isEqualTo(10000L);
        }
    }

    @Nested
    @DisplayName("settle()")
    class Settle {

        @Test
        @DisplayName("PENDING 상태에서 settle() 호출 시 SETTLED로 전이되고 settlementId가 설정된다")
        void settle_success() {
            SettlementTarget target = SettlementTarget.create(
                    UUID.randomUUID(), UUID.randomUUID(), 10000L, LocalDateTime.now());
            UUID settlementId = UUID.randomUUID();

            target.settle(settlementId);

            assertThat(target.getStatus()).isEqualTo(SettlementTargetStatus.SETTLED);
            assertThat(target.getSettlementId()).isEqualTo(settlementId);
        }

        @Test
        @DisplayName("SETTLED 상태에서 settle() 호출 시 예외가 발생한다")
        void settle_alreadySettled_throws() {
            SettlementTarget target = SettlementTarget.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    10000L, LocalDateTime.now(), UUID.randomUUID(), SettlementTargetStatus.SETTLED);

            assertThatThrownBy(() -> target.settle(UUID.randomUUID()))
                    .isInstanceOf(InvalidSettlementTargetStatusException.class);
        }
    }
}
