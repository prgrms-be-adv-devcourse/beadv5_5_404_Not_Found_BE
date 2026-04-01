package com.notfound.member.presentation.controller;

import com.notfound.member.application.port.in.CheckSellerStatusUseCase;
import com.notfound.member.application.port.in.GetSellerAccountUseCase;
import com.notfound.member.domain.model.Seller;
import com.notfound.member.presentation.dto.ApiResponse;
import com.notfound.member.presentation.dto.SellerAccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Internal - Seller", description = "내부 판매자 API (서비스 간 통신)")
@RestController
@RequestMapping("/internal/seller")
public class InternalSellerController {

    private final CheckSellerStatusUseCase checkSellerStatusUseCase;
    private final GetSellerAccountUseCase getSellerAccountUseCase;

    public InternalSellerController(CheckSellerStatusUseCase checkSellerStatusUseCase,
                                    GetSellerAccountUseCase getSellerAccountUseCase) {
        this.checkSellerStatusUseCase = checkSellerStatusUseCase;
        this.getSellerAccountUseCase = getSellerAccountUseCase;
    }

    @Operation(summary = "판매자 권한 확인", description = "판매자 승인 여부 확인 (상품 서비스용)")
    @GetMapping("/{memberId}/approved")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> isApprovedSeller(
            @PathVariable UUID memberId) {

        boolean approved = checkSellerStatusUseCase.isApprovedSeller(memberId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "MEMBER_SELLER_STATUS_CHECK_SUCCESS",
                        "판매자 상태 조회가 완료되었습니다.",
                        Map.of("approved", approved)));
    }

    @Operation(summary = "판매자 계좌 정보 조회", description = "판매자 정산 계좌 정보 (결제 서비스용)")
    @GetMapping("/{memberId}/account")
    public ResponseEntity<ApiResponse<SellerAccountResponse>> getSellerAccount(
            @PathVariable UUID memberId) {

        Seller seller = getSellerAccountUseCase.getSellerAccount(memberId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "SELLER_ACCOUNT_FETCH_SUCCESS",
                        "판매자 계좌 정보 조회에 성공했습니다.",
                        SellerAccountResponse.from(seller)));
    }
}
