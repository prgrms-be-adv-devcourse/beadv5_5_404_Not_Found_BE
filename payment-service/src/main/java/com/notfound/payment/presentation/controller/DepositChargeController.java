package com.notfound.payment.presentation.controller;

import com.notfound.payment.application.port.in.ConfirmDepositChargeUseCase;
import com.notfound.payment.application.port.in.PrepareDepositChargeUseCase;
import com.notfound.payment.domain.exception.PaymentException;
import com.notfound.payment.presentation.ApiResponse;
import com.notfound.payment.presentation.dto.DepositChargeConfirmRequest;
import com.notfound.payment.presentation.dto.DepositChargeConfirmResponse;
import com.notfound.payment.presentation.dto.DepositChargeReadyRequest;
import com.notfound.payment.presentation.dto.DepositChargeReadyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/payment/deposit/charge")
@RequiredArgsConstructor
public class DepositChargeController {

    private final PrepareDepositChargeUseCase prepareDepositChargeUseCase;
    private final ConfirmDepositChargeUseCase confirmDepositChargeUseCase;

    @PostMapping("/ready")
    public ResponseEntity<ApiResponse<DepositChargeReadyResponse>> ready(
            @RequestHeader("X-User-Id") UUID memberId,
            @RequestHeader("X-Email-Verified") boolean emailVerified,
            @RequestBody @Valid DepositChargeReadyRequest request) {

        if (!emailVerified) {
            throw PaymentException.emailNotVerified();
        }

        PrepareDepositChargeUseCase.PrepareResult result =
                prepareDepositChargeUseCase.prepare(request.toCommand(memberId));

        return ResponseEntity.ok(ApiResponse.ok(
                "DEPOSIT_CHARGE_READY",
                "예치금 충전 준비가 완료되었습니다.",
                DepositChargeReadyResponse.from(result)
        ));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<DepositChargeConfirmResponse>> confirm(
            @RequestHeader("X-User-Id") UUID memberId,
            @RequestHeader("X-Email-Verified") boolean emailVerified,
            @RequestBody @Valid DepositChargeConfirmRequest request) {

        if (!emailVerified) {
            throw PaymentException.emailNotVerified();
        }

        ConfirmDepositChargeUseCase.ConfirmResult result =
                confirmDepositChargeUseCase.confirm(request.toCommand(memberId));

        return ResponseEntity.ok(ApiResponse.ok(
                "DEPOSIT_CHARGED",
                "예치금 충전이 완료되었습니다.",
                DepositChargeConfirmResponse.from(result)
        ));
    }
}
