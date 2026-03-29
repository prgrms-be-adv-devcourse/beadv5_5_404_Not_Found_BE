package com.notfound.payment.presentation.controller;

import com.notfound.payment.application.port.in.GetDepositHistoryUseCase;
import com.notfound.payment.domain.model.DepositType;
import com.notfound.payment.presentation.ApiResponse;
import com.notfound.payment.presentation.dto.DepositHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/payment/deposit")
@RequiredArgsConstructor
public class DepositController {

    private final GetDepositHistoryUseCase getDepositHistoryUseCase;

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<DepositHistoryResponse>> getHistory(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestParam(required = false) DepositType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 100) {
            size = 100;
        }

        GetDepositHistoryUseCase.HistoryResult result =
                getDepositHistoryUseCase.getHistory(memberId, type, page, size);

        return ResponseEntity.ok(ApiResponse.ok(
                "DEPOSIT_HISTORY_FOUND",
                "예치금 내역을 조회했습니다.",
                DepositHistoryResponse.from(result)
        ));
    }
}
