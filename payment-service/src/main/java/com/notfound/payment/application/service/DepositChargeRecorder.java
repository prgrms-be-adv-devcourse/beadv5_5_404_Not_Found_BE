package com.notfound.payment.application.service;

import com.notfound.payment.application.port.in.ConfirmDepositChargeUseCase;
import com.notfound.payment.application.port.out.DepositEventPublisher;
import com.notfound.payment.application.port.out.DepositPort;
import com.notfound.payment.application.port.out.MemberPort;
import com.notfound.payment.application.port.out.PaymentPort;
import com.notfound.payment.application.port.out.PgPort;
import com.notfound.payment.domain.event.DepositChargedEvent;
import com.notfound.payment.domain.model.Deposit;
import com.notfound.payment.domain.model.DepositType;
import com.notfound.payment.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * PG 승인 성공 이후 DB 반영을 단일 트랜잭션으로 처리한다.
 * confirm() 외부에서 PG 호출 후 이 클래스를 통해 커밋하여
 * DB 커넥션 점유 시간을 최소화한다.
 */
@Component
@RequiredArgsConstructor
class DepositChargeRecorder {

    private final PaymentPort paymentPort;
    private final DepositPort depositPort;
    private final MemberPort memberPort;
    private final DepositEventPublisher depositEventPublisher;

    @Transactional
    public ConfirmDepositChargeUseCase.ConfirmResult record(
            Payment payment,
            UUID memberId,
            PgPort.PgConfirmResult pgResult
    ) {
        payment.complete(pgResult.pgTransactionId(), pgResult.paymentKey(), pgResult.approvedAt());
        paymentPort.save(payment);

        int currentBalance = memberPort.getDepositBalance(memberId);
        int balanceAfter = currentBalance + payment.getAmount();

        Deposit deposit = Deposit.create(
                memberId,
                payment.getId(),
                null,
                DepositType.CHARGE,
                payment.getAmount(),
                balanceAfter,
                "예치금 충전"
        );
        Deposit savedDeposit = depositPort.save(deposit);
        depositEventPublisher.publishDepositCharged(
                new DepositChargedEvent(memberId, payment.getAmount(), balanceAfter, savedDeposit.getId().toString()));

        return new ConfirmDepositChargeUseCase.ConfirmResult(
                payment.getId(),
                payment.getAmount(),
                balanceAfter,
                pgResult.pgTransactionId(),
                pgResult.method(),
                pgResult.approvedAt()
        );
    }
}
