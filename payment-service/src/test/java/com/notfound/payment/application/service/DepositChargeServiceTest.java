package com.notfound.payment.application.service;

import com.notfound.payment.application.port.in.ConfirmDepositChargeUseCase.ConfirmCommand;
import com.notfound.payment.application.port.in.ConfirmDepositChargeUseCase.ConfirmResult;
import com.notfound.payment.application.port.in.PrepareDepositChargeUseCase.PrepareCommand;
import com.notfound.payment.application.port.in.PrepareDepositChargeUseCase.PrepareResult;
import com.notfound.payment.application.port.out.MemberPort;
import com.notfound.payment.application.port.out.PaymentPort;
import com.notfound.payment.application.port.out.PgPort;
import com.notfound.payment.domain.exception.PaymentException;
import com.notfound.payment.domain.model.Payment;
import com.notfound.payment.domain.model.PaymentMethodType;
import com.notfound.payment.domain.model.PaymentPurpose;
import com.notfound.payment.domain.model.PaymentStatus;
import com.notfound.payment.domain.model.PgProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DepositChargeServiceTest {

    @Mock private PaymentPort paymentPort;
    @Mock private MemberPort memberPort;
    @Mock private PgPort pgPort;
    @Mock private PaymentStatusUpdater paymentStatusUpdater;
    @Mock private DepositChargeRecorder depositChargeRecorder;

    @InjectMocks
    private DepositChargeService depositChargeService;

    // ===== prepare =====

    @Test
    void prepare_금액이_최솟값_미만이면_예외() {
        PrepareCommand command = new PrepareCommand(UUID.randomUUID(), 999);

        assertThatThrownBy(() -> depositChargeService.prepare(command))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("충전 금액");
    }

    @Test
    void prepare_금액이_최댓값_초과이면_예외() {
        PrepareCommand command = new PrepareCommand(UUID.randomUUID(), 500001);

        assertThatThrownBy(() -> depositChargeService.prepare(command))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("충전 금액");
    }

    @Test
    void prepare_비활성_회원이면_예외() {
        UUID memberId = UUID.randomUUID();
        PrepareCommand command = new PrepareCommand(memberId, 10000);
        given(memberPort.existsActiveMember(memberId)).willReturn(false);

        assertThatThrownBy(() -> depositChargeService.prepare(command))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("활성 상태");
    }

    @Test
    void prepare_잔액_한도_초과이면_예외() {
        UUID memberId = UUID.randomUUID();
        PrepareCommand command = new PrepareCommand(memberId, 10000);
        given(memberPort.existsActiveMember(memberId)).willReturn(true);
        given(memberPort.getDepositBalance(memberId)).willReturn(995000);

        assertThatThrownBy(() -> depositChargeService.prepare(command))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("한도");
    }

    @Test
    void prepare_정상_요청이면_Payment_저장_후_결과_반환() {
        UUID memberId = UUID.randomUUID();
        PrepareCommand command = new PrepareCommand(memberId, 50000);
        Payment savedPayment = Payment.create(memberId, null, PgProvider.TOSS, 50000,
                PaymentMethodType.PG, PaymentPurpose.DEPOSIT_CHARGE, "DEPOSIT-20260330-ABCD1234");

        given(memberPort.existsActiveMember(memberId)).willReturn(true);
        given(memberPort.getDepositBalance(memberId)).willReturn(0);
        given(paymentPort.save(any())).willReturn(savedPayment);
        given(pgPort.getConfig()).willReturn(new PgPort.PgConfig("clientKey", "http://success", "http://fail"));

        PrepareResult result = depositChargeService.prepare(command);

        assertThat(result.amount()).isEqualTo(50000);
        assertThat(result.pgData().clientKey()).isEqualTo("clientKey");
        then(paymentPort).should().save(any(Payment.class));
    }

    // ===== confirm =====

    @Test
    void confirm_결제_준비_정보_없으면_예외() {
        ConfirmCommand command = new ConfirmCommand(UUID.randomUUID(), "pk", "DEPOSIT-orderId", 10000);
        given(paymentPort.findByIdempotencyKey("DEPOSIT-orderId")).willReturn(Optional.empty());

        assertThatThrownBy(() -> depositChargeService.confirm(command))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("결제 준비 정보");
    }

    @Test
    void confirm_다른_회원의_결제이면_예외() {
        UUID paymentOwner = UUID.randomUUID();
        UUID requester = UUID.randomUUID();
        Payment payment = Payment.of(UUID.randomUUID(), paymentOwner, null, PgProvider.TOSS, 10000,
                PaymentStatus.PENDING, null, null, PaymentMethodType.PG, PaymentPurpose.DEPOSIT_CHARGE, null, "DEPOSIT-orderId");
        given(paymentPort.findByIdempotencyKey("DEPOSIT-orderId")).willReturn(Optional.of(payment));

        assertThatThrownBy(() -> depositChargeService.confirm(new ConfirmCommand(requester, "pk", "DEPOSIT-orderId", 10000)))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("결제 준비 정보");
    }

    @Test
    void confirm_이미_COMPLETED이면_예외() {
        UUID memberId = UUID.randomUUID();
        Payment payment = Payment.of(UUID.randomUUID(), memberId, null, PgProvider.TOSS, 10000,
                PaymentStatus.COMPLETED, "txId", "pk", PaymentMethodType.PG, PaymentPurpose.DEPOSIT_CHARGE, LocalDateTime.now(), "DEPOSIT-orderId");
        given(paymentPort.findByIdempotencyKey("DEPOSIT-orderId")).willReturn(Optional.of(payment));

        assertThatThrownBy(() -> depositChargeService.confirm(new ConfirmCommand(memberId, "pk", "DEPOSIT-orderId", 10000)))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("이미 승인");
    }

    @Test
    void confirm_PENDING_아닌_상태이면_예외() {
        UUID memberId = UUID.randomUUID();
        Payment payment = Payment.of(UUID.randomUUID(), memberId, null, PgProvider.TOSS, 10000,
                PaymentStatus.FAILED, null, null, PaymentMethodType.PG, PaymentPurpose.DEPOSIT_CHARGE, null, "DEPOSIT-orderId");
        given(paymentPort.findByIdempotencyKey("DEPOSIT-orderId")).willReturn(Optional.of(payment));

        assertThatThrownBy(() -> depositChargeService.confirm(new ConfirmCommand(memberId, "pk", "DEPOSIT-orderId", 10000)))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("완료 처리");
    }

    @Test
    void confirm_금액_불일치이면_예외() {
        UUID memberId = UUID.randomUUID();
        Payment payment = Payment.of(UUID.randomUUID(), memberId, null, PgProvider.TOSS, 10000,
                PaymentStatus.PENDING, null, null, PaymentMethodType.PG, PaymentPurpose.DEPOSIT_CHARGE, null, "DEPOSIT-orderId");
        given(paymentPort.findByIdempotencyKey("DEPOSIT-orderId")).willReturn(Optional.of(payment));

        assertThatThrownBy(() -> depositChargeService.confirm(new ConfirmCommand(memberId, "pk", "DEPOSIT-orderId", 99999)))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("금액");
    }

    @Test
    void confirm_PG_PaymentException_발생시_FAILED_처리_후_예외_전파() {
        UUID memberId = UUID.randomUUID();
        Payment payment = Payment.of(UUID.randomUUID(), memberId, null, PgProvider.TOSS, 10000,
                PaymentStatus.PENDING, null, null, PaymentMethodType.PG, PaymentPurpose.DEPOSIT_CHARGE, null, "DEPOSIT-orderId");
        given(paymentPort.findByIdempotencyKey("DEPOSIT-orderId")).willReturn(Optional.of(payment));
        given(pgPort.confirm(any())).willThrow(PaymentException.pgConfirmFailed());

        assertThatThrownBy(() -> depositChargeService.confirm(new ConfirmCommand(memberId, "pk", "DEPOSIT-orderId", 10000)))
                .isInstanceOf(PaymentException.class);
        then(paymentStatusUpdater).should().savePaymentFailed(payment);
    }

    @Test
    void confirm_PG_일반_Exception_발생시_FAILED_처리_후_pgConfirmFailed_예외_전파() {
        UUID memberId = UUID.randomUUID();
        Payment payment = Payment.of(UUID.randomUUID(), memberId, null, PgProvider.TOSS, 10000,
                PaymentStatus.PENDING, null, null, PaymentMethodType.PG, PaymentPurpose.DEPOSIT_CHARGE, null, "DEPOSIT-orderId");
        given(paymentPort.findByIdempotencyKey("DEPOSIT-orderId")).willReturn(Optional.of(payment));
        given(pgPort.confirm(any())).willThrow(new RuntimeException("network error"));

        assertThatThrownBy(() -> depositChargeService.confirm(new ConfirmCommand(memberId, "pk", "DEPOSIT-orderId", 10000)))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("PG 결제 승인");
        then(paymentStatusUpdater).should().savePaymentFailed(payment);
    }

    @Test
    void confirm_PG_승인_성공시_record_호출_후_결과_반환() {
        UUID memberId = UUID.randomUUID();
        Payment payment = Payment.of(UUID.randomUUID(), memberId, null, PgProvider.TOSS, 10000,
                PaymentStatus.PENDING, null, null, PaymentMethodType.PG, PaymentPurpose.DEPOSIT_CHARGE, null, "DEPOSIT-orderId");
        PgPort.PgConfirmResult pgResult = new PgPort.PgConfirmResult("txId", "pk", LocalDateTime.now(), "카드");
        ConfirmResult expected = new ConfirmResult(payment.getId(), 10000, 10000, "txId", "카드", LocalDateTime.now());

        given(paymentPort.findByIdempotencyKey("DEPOSIT-orderId")).willReturn(Optional.of(payment));
        given(pgPort.confirm(any())).willReturn(pgResult);
        given(depositChargeRecorder.record(payment, memberId, pgResult)).willReturn(expected);

        ConfirmResult result = depositChargeService.confirm(new ConfirmCommand(memberId, "pk", "DEPOSIT-orderId", 10000));

        assertThat(result).isEqualTo(expected);
        then(depositChargeRecorder).should().record(payment, memberId, pgResult);
    }

}
