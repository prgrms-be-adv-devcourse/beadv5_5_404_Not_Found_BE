package com.notfound.payment.application.port.out;

import com.notfound.payment.domain.model.Deposit;
import com.notfound.payment.domain.model.DepositType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface DepositPort {

    Deposit save(Deposit deposit);

    Optional<Deposit> findById(UUID id);

    Page<Deposit> findByMemberId(UUID memberId, Pageable pageable);

    Page<Deposit> findByMemberIdAndType(UUID memberId, DepositType type, Pageable pageable);

    Optional<Deposit> findByPaymentId(UUID paymentId);
}
