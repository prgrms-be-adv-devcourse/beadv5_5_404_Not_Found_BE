package com.notfound.payment.presentation.controller;

import com.notfound.payment.application.port.in.DeductDepositUseCase;
import com.notfound.payment.application.port.in.RefundDepositUseCase;
import com.notfound.payment.presentation.ApiResponse;
import com.notfound.payment.presentation.dto.DepositDeductRequest;
import com.notfound.payment.presentation.dto.DepositDeductResponse;
import com.notfound.payment.presentation.dto.DepositRefundRequest;
import com.notfound.payment.presentation.dto.DepositRefundResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/deposit")
@RequiredArgsConstructor
public class InternalDepositController {

    private final DeductDepositUseCase deductDepositUseCase;
    private final RefundDepositUseCase refundDepositUseCase;

    /** Order 서비스가 주문 결제 시 호출 */
    @PostMapping("/deduct")
    public ResponseEntity<ApiResponse<DepositDeductResponse>> deduct(
            @RequestBody @Valid DepositDeductRequest request) {

        DeductDepositUseCase.DeductResult result = deductDepositUseCase.deduct(request.toCommand());
        return ResponseEntity.ok(ApiResponse.ok(
                "DEPOSIT_DEDUCTED",
                "예치금이 차감되었습니다.",
                DepositDeductResponse.from(result)
        ));
    }

    /** Order 서비스가 주문 취소 시 호출 */
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<DepositRefundResponse>> refund(
            @RequestBody @Valid DepositRefundRequest request) {

        RefundDepositUseCase.RefundResult result = refundDepositUseCase.refund(request.toCommand());
        return ResponseEntity.ok(ApiResponse.ok(
                "DEPOSIT_REFUNDED",
                "예치금이 환급되었습니다.",
                DepositRefundResponse.from(result)
        ));
    }
}
