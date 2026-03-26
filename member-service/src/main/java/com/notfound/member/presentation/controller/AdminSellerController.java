package com.notfound.member.presentation.controller;

import com.notfound.member.application.port.in.ApproveSellerUseCase;
import com.notfound.member.domain.model.Seller;
import com.notfound.member.presentation.dto.ApiResponse;
import com.notfound.member.presentation.dto.RegisterSellerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/seller")
public class AdminSellerController {

    private final ApproveSellerUseCase approveSellerUseCase;

    public AdminSellerController(ApproveSellerUseCase approveSellerUseCase) {
        this.approveSellerUseCase = approveSellerUseCase;
    }

    @PatchMapping("/{sellerId}/approve")
    public ResponseEntity<ApiResponse<RegisterSellerResponse>> approveSeller(
            @PathVariable UUID sellerId) {

        // ADMIN role 검증은 RoleAuthorizationFilter에서 처리
        Seller seller = approveSellerUseCase.approveSeller(sellerId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "SELLER_APPROVE_SUCCESS",
                        "판매자가 승인되었습니다.", RegisterSellerResponse.from(seller)));
    }
}
