package com.notfound.settlement.adapter.in.rest;

import com.notfound.settlement.adapter.in.rest.dto.ApiResponse;
import com.notfound.settlement.adapter.in.rest.dto.SettlementErrorCode;
import com.notfound.settlement.adapter.in.rest.dto.SettlementResponse;
import com.notfound.settlement.application.port.in.GetSettlementHistoryUseCase;
import com.notfound.settlement.domain.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final GetSettlementHistoryUseCase getSettlementHistoryUseCase;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getMySettlements(
            @AuthUser AuthenticatedUser user) {
        requireSeller(user);
        UUID sellerId = UUID.fromString(user.userId());
        List<SettlementResponse> response = getSettlementHistoryUseCase.getSettlements(sellerId)
                .stream()
                .map(SettlementResponse::from)
                .toList();
        SettlementErrorCode code = SettlementErrorCode.SETTLEMENT_LIST_GET_SUCCESS;
        return ResponseEntity.ok(
                ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(), response));
    }

    private void requireSeller(AuthenticatedUser user) {
        if (user == null || !"SELLER".equals(user.role())) {
            throw new ForbiddenException(SettlementErrorCode.FORBIDDEN.getMessage());
        }
        // TODO: issue #30 — member-service 이메일 인증 구현 후 아래 체크 추가
        // if (!user.emailVerified()) {
        //     throw new ForbiddenException(SettlementErrorCode.EMAIL_NOT_VERIFIED.getMessage());
        // }
    }
}
