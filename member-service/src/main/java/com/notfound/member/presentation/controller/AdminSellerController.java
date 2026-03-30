package com.notfound.member.presentation.controller;

import com.notfound.member.application.port.in.ApproveSellerUseCase;
import com.notfound.member.domain.model.Seller;
import com.notfound.member.domain.model.SellerStatus;
import com.notfound.member.presentation.dto.ApiResponse;
import com.notfound.member.presentation.dto.UpdateSellerStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Admin Seller", description = "관리자 판매자 관리 API")
@RestController
@RequestMapping("/member/admin/seller")
public class AdminSellerController {

    private final ApproveSellerUseCase approveSellerUseCase;

    public AdminSellerController(ApproveSellerUseCase approveSellerUseCase) {
        this.approveSellerUseCase = approveSellerUseCase;
    }

    @Operation(summary = "판매자 승인/거절", description = "판매자 상태 변경")
    @PatchMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateSellerStatus(
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateSellerStatusRequest request) {

        SellerStatus status = SellerStatus.valueOf(request.status());
        Seller seller = approveSellerUseCase.updateSellerStatus(memberId, status);

        return ResponseEntity.ok(
                ApiResponse.success(200, "SELLER_STATUS_UPDATE_SUCCESS",
                        "판매자 상태가 변경되었습니다.",
                        Map.of("memberId", seller.getMemberId(),
                                "sellerStatus", seller.getStatus().name())));
    }
}
