package com.notfound.payment.application.service;

import com.notfound.payment.application.port.in.DeductDepositUseCase;
import com.notfound.payment.application.port.in.RefundDepositUseCase;
import com.notfound.payment.application.port.out.DepositEventPublisher;
import com.notfound.payment.application.port.out.DepositPort;
import com.notfound.payment.application.port.out.MemberPort;
import com.notfound.payment.domain.event.DepositDeductedEvent;
import com.notfound.payment.domain.event.DepositRefundedEvent;
import com.notfound.payment.domain.exception.PaymentException;
import com.notfound.payment.domain.model.Deposit;
import com.notfound.payment.domain.model.DepositType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DepositTransactionServiceTest {

    @Mock private DepositPort depositPort;
    @Mock private MemberPort memberPort;
    @Mock private DepositEventPublisher depositEventPublisher;

    @InjectMocks
    private DepositTransactionService depositTransactionService;

    // ===== deduct =====

    @Test
    void deduct_잔액_부족이면_예외() {
        UUID memberId = UUID.randomUUID();
        given(memberPort.getDepositBalance(memberId)).willReturn(10000);

        assertThatThrownBy(() -> depositTransactionService.deduct(
                new DeductDepositUseCase.DeductCommand(memberId, UUID.randomUUID(), 20000, "결제")))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("잔액이 부족");

        then(depositPort).shouldHaveNoInteractions();
        then(memberPort).should().getDepositBalance(memberId);
    }

    @Test
    void deduct_잔액과_차감액이_같으면_성공() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Deposit saved = Deposit.create(memberId, null, orderId, DepositType.USE, 25000, 0, "결제");
        given(memberPort.getDepositBalance(memberId)).willReturn(25000);
        given(depositPort.save(any())).willReturn(saved);

        DeductDepositUseCase.DeductResult result = depositTransactionService.deduct(
                new DeductDepositUseCase.DeductCommand(memberId, orderId, 25000, "결제"));

        assertThat(result.balanceAfter()).isEqualTo(0);
    }

    @Test
    void deduct_정상_차감시_balanceAfter_정확히_계산() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Deposit saved = Deposit.create(memberId, null, orderId, DepositType.USE, 25000, 75000, "결제");
        given(memberPort.getDepositBalance(memberId)).willReturn(100000);
        given(depositPort.save(any())).willReturn(saved);

        DeductDepositUseCase.DeductResult result = depositTransactionService.deduct(
                new DeductDepositUseCase.DeductCommand(memberId, orderId, 25000, "결제"));

        assertThat(result.balanceAfter()).isEqualTo(75000);
    }

    @Test
    void deduct_정상_차감시_Deposit_저장_및_이벤트_발행() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Deposit saved = Deposit.create(memberId, null, orderId, DepositType.USE, 25000, 75000, "결제");
        given(memberPort.getDepositBalance(memberId)).willReturn(100000);
        given(depositPort.save(any())).willReturn(saved);

        depositTransactionService.deduct(
                new DeductDepositUseCase.DeductCommand(memberId, orderId, 25000, "결제"));

        then(depositPort).should().save(any(Deposit.class));
        then(depositEventPublisher).should().publishDepositDeducted(any(DepositDeductedEvent.class));
    }

    // ===== refund =====

    @Test
    void refund_정상_환급시_balanceAfter_정확히_계산() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Deposit saved = Deposit.create(memberId, null, orderId, DepositType.REFUND, 25000, 125000, "환급");
        given(memberPort.getDepositBalance(memberId)).willReturn(100000);
        given(depositPort.save(any())).willReturn(saved);

        RefundDepositUseCase.RefundResult result = depositTransactionService.refund(
                new RefundDepositUseCase.RefundCommand(memberId, orderId, 25000, "환급"));

        assertThat(result.balanceAfter()).isEqualTo(125000);
    }

    @Test
    void refund_정상_환급시_Deposit_저장_및_이벤트_발행() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Deposit saved = Deposit.create(memberId, null, orderId, DepositType.REFUND, 25000, 125000, "환급");
        given(memberPort.getDepositBalance(memberId)).willReturn(100000);
        given(depositPort.save(any())).willReturn(saved);

        depositTransactionService.refund(
                new RefundDepositUseCase.RefundCommand(memberId, orderId, 25000, "환급"));

        then(depositPort).should().save(any(Deposit.class));
        then(depositEventPublisher).should().publishDepositRefunded(any(DepositRefundedEvent.class));
    }
}
