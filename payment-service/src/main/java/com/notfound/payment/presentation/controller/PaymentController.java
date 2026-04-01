package com.notfound.payment.presentation.controller;

import com.notfound.payment.application.port.in.PayOrderUseCase;
import com.notfound.payment.domain.exception.PaymentException;
import com.notfound.payment.presentation.ApiResponse;
import com.notfound.payment.presentation.dto.PayOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/payment/orders")
@RequiredArgsConstructor
public class PaymentController {

    private final PayOrderUseCase payOrderUseCase;

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<PayOrderResponse>> pay(
            @RequestHeader("X-User-Id") UUID memberId,
            @RequestHeader("X-Email-Verified") boolean emailVerified,
            @PathVariable UUID orderId) {

        if (!emailVerified) {
            throw PaymentException.emailNotVerified();
        }

        PayOrderUseCase.PayResult result = payOrderUseCase.pay(
                new PayOrderUseCase.PayCommand(memberId, orderId)
        );

        return ResponseEntity.ok(ApiResponse.ok(
                "ORDER_PAID",
                "결제가 완료되었습니다.",
                PayOrderResponse.from(result)
        ));
    }
}
