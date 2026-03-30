package com.notfound.payment.application.service;

import com.notfound.payment.application.port.in.DeductDepositUseCase;
import com.notfound.payment.application.port.in.RefundDepositUseCase;
import com.notfound.payment.application.port.out.DepositPort;
import com.notfound.payment.application.port.out.MemberPort;
import com.notfound.payment.domain.exception.PaymentException;
import com.notfound.payment.domain.model.Deposit;
import com.notfound.payment.domain.model.DepositType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepositTransactionService implements DeductDepositUseCase, RefundDepositUseCase {

    private final DepositPort depositPort;
    private final MemberPort memberPort;

    @Override
    @Transactional
    public DeductResult deduct(DeductCommand command) {
        int currentBalance = memberPort.getDepositBalance(command.memberId());
        if (currentBalance < command.amount()) {
            throw PaymentException.depositInsufficientBalance();
        }

        int balanceAfter = currentBalance - command.amount();
        Deposit deposit = Deposit.create(
                command.memberId(),
                null,
                command.orderId(),
                DepositType.USE,
                command.amount(),
                balanceAfter,
                command.description()
        );
        Deposit saved = depositPort.save(deposit);
        memberPort.deductDeposit(command.memberId(), command.amount());

        return new DeductResult(saved.getId(), balanceAfter);
    }

    @Override
    @Transactional
    public RefundResult refund(RefundCommand command) {
        int currentBalance = memberPort.getDepositBalance(command.memberId());
        int balanceAfter = currentBalance + command.amount();

        Deposit deposit = Deposit.create(
                command.memberId(),
                null,
                command.orderId(),
                DepositType.REFUND,
                command.amount(),
                balanceAfter,
                command.description()
        );
        Deposit saved = depositPort.save(deposit);
        memberPort.chargeDeposit(command.memberId(), command.amount());

        return new RefundResult(saved.getId(), balanceAfter);
    }
}
