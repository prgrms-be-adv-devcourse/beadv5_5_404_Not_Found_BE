package com.notfound.payment.application.port.out;

import com.notfound.payment.domain.model.Refund;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefundPort {

    Refund save(Refund refund);

    Optional<Refund> findById(UUID id);

    List<Refund> findByPaymentId(UUID paymentId);

    List<Refund> findByOrderItemId(UUID orderItemId);
}
