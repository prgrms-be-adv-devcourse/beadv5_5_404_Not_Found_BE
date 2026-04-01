package com.notfound.payment.application.service;

import com.notfound.payment.application.port.out.PaymentPort;
import com.notfound.payment.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentStatusUpdater {

    private final PaymentPort paymentPort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePaymentFailed(Payment payment) {
        payment.fail();
        paymentPort.save(payment);
    }
}
