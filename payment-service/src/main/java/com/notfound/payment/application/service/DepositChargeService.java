package com.notfound.payment.application.service;

import com.notfound.payment.application.port.in.ConfirmDepositChargeUseCase;
import com.notfound.payment.application.port.in.PrepareDepositChargeUseCase;
import com.notfound.payment.application.port.out.MemberPort;
import com.notfound.payment.application.port.out.PaymentPort;
import com.notfound.payment.application.port.out.PgPort;
import com.notfound.payment.domain.exception.PaymentException;
import com.notfound.payment.domain.model.Payment;
import com.notfound.payment.domain.model.PaymentMethodType;
import com.notfound.payment.domain.model.PaymentPurpose;
import com.notfound.payment.domain.model.PaymentStatus;
import com.notfound.payment.domain.model.PgProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositChargeService implements PrepareDepositChargeUseCase, ConfirmDepositChargeUseCase {

    private static final int DEPOSIT_MIN_CHARGE_AMOUNT = 1000;
    private static final int DEPOSIT_MAX_CHARGE_AMOUNT = 500000;
    private static final int DEPOSIT_MAX_BALANCE = 1000000;

    private final PaymentPort paymentPort;
    private final MemberPort memberPort;
    private final PgPort pgPort;
    private final PaymentStatusUpdater paymentStatusUpdater;
    private final DepositChargeRecorder depositChargeRecorder;

    @Override
    @Transactional
    public PrepareResult prepare(PrepareCommand command) {
        validateChargeAmount(command.amount());

        if (!memberPort.existsActiveMember(command.memberId())) {
            throw PaymentException.memberNotActive();
        }

        int currentBalance = memberPort.getDepositBalance(command.memberId());
        if (currentBalance + command.amount() > DEPOSIT_MAX_BALANCE) {
            throw PaymentException.depositBalanceExceedsLimit();
        }

        String idempotencyKey = generateIdempotencyKey();
        Payment payment = Payment.create(
                command.memberId(),
                null,
                PgProvider.TOSS,
                command.amount(),
                PaymentMethodType.PG,
                PaymentPurpose.DEPOSIT_CHARGE,
                idempotencyKey
        );
        payment = paymentPort.save(payment);

        PgPort.PgConfig pgConfig = pgPort.getConfig();
        return new PrepareResult(
                payment.getId(),
                payment.getAmount(),
                PgProvider.TOSS.name(),
                new PrepareResult.PgData(
                        pgConfig.clientKey(),
                        idempotencyKey,
                        payment.getAmount(),
                        "예치금 충전",
                        pgConfig.successUrl(),
                        pgConfig.failUrl()
                )
        );
    }

    @Override
    public ConfirmResult confirm(ConfirmCommand command) {
        // 1. 조회 + 상태/금액 검증 (트랜잭션 밖)
        Payment payment = paymentPort.findByIdempotencyKey(command.orderId())
                .orElseThrow(PaymentException::paymentReadyNotFound);

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw PaymentException.paymentAlreadyConfirmed();
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw PaymentException.paymentNotCompletable();
        }
        if (payment.getAmount() != command.amount()) {
            throw PaymentException.amountMismatch();
        }

        // 2. Toss 승인 API 호출 (외부 HTTP — 트랜잭션 밖)
        PgPort.PgConfirmResult pgResult;
        try {
            pgResult = pgPort.confirm(new PgPort.PgConfirmCommand(
                    command.paymentKey(),
                    command.orderId(),
                    command.amount()
            ));
        } catch (PaymentException e) {
            paymentStatusUpdater.savePaymentFailed(payment);
            throw e;
        } catch (Exception e) {
            paymentStatusUpdater.savePaymentFailed(payment);
            throw PaymentException.pgConfirmFailed(e);
        }

        // 3. 성공: Payment COMPLETED + Deposit 이력 + DepositChargedEvent (@Transactional)
        return depositChargeRecorder.record(payment, command.memberId(), pgResult);
    }

    private void validateChargeAmount(int amount) {
        if (amount < DEPOSIT_MIN_CHARGE_AMOUNT || amount > DEPOSIT_MAX_CHARGE_AMOUNT) {
            throw PaymentException.invalidChargeAmount();
        }
    }

    private String generateIdempotencyKey() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "DEPOSIT-" + date + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
