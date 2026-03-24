package com.notfound.payment.application.port.out;

import com.notfound.payment.domain.model.Deposit;
import com.notfound.payment.domain.model.DepositType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepositPort {

    Deposit save(Deposit deposit);

    Optional<Deposit> findById(UUID id);

    List<Deposit> findByMemberId(UUID memberId);

    List<Deposit> findByMemberIdAndType(UUID memberId, DepositType type);

    Optional<Deposit> findByPaymentId(UUID paymentId);
}
