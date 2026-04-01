package com.notfound.payment.application.service;

import com.notfound.payment.application.port.in.ConfirmDepositChargeUseCase.ConfirmResult;
import com.notfound.payment.application.port.out.DepositEventPublisher;
import com.notfound.payment.application.port.out.DepositPort;
import com.notfound.payment.application.port.out.MemberPort;
import com.notfound.payment.application.port.out.PaymentPort;
import com.notfound.payment.application.port.out.PgPort;
import com.notfound.payment.domain.event.DepositChargedEvent;
import com.notfound.payment.domain.model.Deposit;
import com.notfound.payment.domain.model.DepositType;
import com.notfound.payment.domain.model.Payment;
import com.notfound.payment.domain.model.PaymentMethodType;
import com.notfound.payment.domain.model.PaymentPurpose;
import com.notfound.payment.domain.model.PaymentStatus;
import com.notfound.payment.domain.model.PgProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DepositChargeRecorderTest {

    @Mock private PaymentPort paymentPort;
    @Mock private DepositPort depositPort;
    @Mock private MemberPort memberPort;
    @Mock private DepositEventPublisher depositEventPublisher;

    @InjectMocks
    private DepositChargeRecorder depositChargeRecorder;

    @Test
    void record_Payment_COMPLETED_처리_및_Deposit_저장() {
        UUID memberId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        LocalDateTime approvedAt = LocalDateTime.now();

        Payment payment = Payment.of(paymentId, memberId, null, PgProvider.TOSS, 30000,
                PaymentStatus.PENDING, null, null, PaymentMethodType.PG, PaymentPurpose.DEPOSIT_CHARGE, null, "DEPOSIT-key");
        PgPort.PgConfirmResult pgResult = new PgPort.PgConfirmResult("txId", "paymentKey", approvedAt, "카드");
        Deposit savedDeposit = Deposit.create(memberId, paymentId, null, DepositType.CHARGE, 30000, 80000, "예치금 충전");

        given(memberPort.getDepositBalance(memberId)).willReturn(50000);
        given(paymentPort.save(any())).willReturn(payment);
        given(depositPort.save(any())).willReturn(savedDeposit);

        ConfirmResult result = depositChargeRecorder.record(payment, memberId, pgResult);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getPgTransactionId()).isEqualTo("txId");
        assertThat(payment.getPaymentKey()).isEqualTo("paymentKey");
        assertThat(result.chargedAmount()).isEqualTo(30000);
        assertThat(result.balanceAfter()).isEqualTo(80000);
        assertThat(result.pgTransactionId()).isEqualTo("txId");
        assertThat(result.method()).isEqualTo("카드");
    }

    @Test
    void record_잔액이_0일때_충전하면_balanceAfter_정확히_계산() {
        UUID memberId = UUID.randomUUID();
        Payment payment = Payment.of(UUID.randomUUID(), memberId, null, PgProvider.TOSS, 50000,
                PaymentStatus.PENDING, null, null, PaymentMethodType.PG, PaymentPurpose.DEPOSIT_CHARGE, null, "DEPOSIT-key");
        PgPort.PgConfirmResult pgResult = new PgPort.PgConfirmResult("txId", "paymentKey", LocalDateTime.now(), "카드");
        Deposit savedDeposit = Deposit.create(memberId, payment.getId(), null, DepositType.CHARGE, 50000, 50000, "예치금 충전");

        given(memberPort.getDepositBalance(memberId)).willReturn(0);
        given(paymentPort.save(any())).willReturn(payment);
        given(depositPort.save(any())).willReturn(savedDeposit);

        ConfirmResult result = depositChargeRecorder.record(payment, memberId, pgResult);

        assertThat(result.balanceAfter()).isEqualTo(50000);
    }

    @Test
    void record_DepositChargedEvent_올바른_값으로_발행() {
        UUID memberId = UUID.randomUUID();
        Payment payment = Payment.of(UUID.randomUUID(), memberId, null, PgProvider.TOSS, 30000,
                PaymentStatus.PENDING, null, null, PaymentMethodType.PG, PaymentPurpose.DEPOSIT_CHARGE, null, "DEPOSIT-key");
        PgPort.PgConfirmResult pgResult = new PgPort.PgConfirmResult("txId", "paymentKey", LocalDateTime.now(), "카드");
        Deposit savedDeposit = Deposit.create(memberId, payment.getId(), null, DepositType.CHARGE, 30000, 80000, "예치금 충전");

        given(memberPort.getDepositBalance(memberId)).willReturn(50000);
        given(paymentPort.save(any())).willReturn(payment);
        given(depositPort.save(any())).willReturn(savedDeposit);

        depositChargeRecorder.record(payment, memberId, pgResult);

        ArgumentCaptor<DepositChargedEvent> captor = ArgumentCaptor.forClass(DepositChargedEvent.class);
        then(depositEventPublisher).should().publishDepositCharged(captor.capture());

        DepositChargedEvent event = captor.getValue();
        assertThat(event.memberId()).isEqualTo(memberId);
        assertThat(event.chargedAmount()).isEqualTo(30000);
        assertThat(event.balanceAfter()).isEqualTo(80000);
    }
}
