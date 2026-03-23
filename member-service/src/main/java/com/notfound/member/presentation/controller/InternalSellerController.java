package com.notfound.member.presentation.controller;

import com.notfound.member.application.port.in.CheckSellerStatusUseCase;
import com.notfound.member.presentation.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/seller")
public class InternalSellerController {

    private final CheckSellerStatusUseCase checkSellerStatusUseCase;

    public InternalSellerController(CheckSellerStatusUseCase checkSellerStatusUseCase) {
        this.checkSellerStatusUseCase = checkSellerStatusUseCase;
    }

    @GetMapping("/{memberId}/approved")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> isApprovedSeller(
            @PathVariable UUID memberId) {

        boolean approved = checkSellerStatusUseCase.isApprovedSeller(memberId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "MEMBER_SELLER_STATUS_CHECK_SUCCESS",
                        "판매자 상태 조회가 완료되었습니다.",
                        Map.of("approved", approved)));
    }
}
