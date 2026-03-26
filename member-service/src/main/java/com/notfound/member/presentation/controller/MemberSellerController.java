package com.notfound.member.presentation.controller;

import com.notfound.member.application.port.in.GetSellerAccountUseCase;
import com.notfound.member.application.port.in.RegisterSellerUseCase;
import com.notfound.member.domain.model.Seller;
import com.notfound.member.infrastructure.security.AuthUser;
import com.notfound.member.infrastructure.security.AuthenticatedUser;
import com.notfound.member.presentation.dto.ApiResponse;
import com.notfound.member.presentation.dto.RegisterSellerRequest;
import com.notfound.member.presentation.dto.RegisterSellerResponse;
import com.notfound.member.presentation.dto.SellerProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/member/seller")
public class MemberSellerController {

    private final RegisterSellerUseCase registerSellerUseCase;
    private final GetSellerAccountUseCase getSellerAccountUseCase;

    public MemberSellerController(RegisterSellerUseCase registerSellerUseCase,
                                  GetSellerAccountUseCase getSellerAccountUseCase) {
        this.registerSellerUseCase = registerSellerUseCase;
        this.getSellerAccountUseCase = getSellerAccountUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RegisterSellerResponse>> registerSeller(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody RegisterSellerRequest request) {

        UUID memberId = UUID.fromString(user.userId());
        Seller seller = registerSellerUseCase.registerSeller(memberId, request.toCommand());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "SELLER_REGISTER_SUCCESS",
                        "판매자 등록 신청이 완료되었습니다.", RegisterSellerResponse.from(seller)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SellerProfileResponse>> getMySellerProfile(
            @AuthUser AuthenticatedUser user) {

        UUID memberId = UUID.fromString(user.userId());
        Seller seller = getSellerAccountUseCase.getSellerAccount(memberId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "SELLER_PROFILE_FETCH_SUCCESS",
                        "판매자 정보 조회에 성공했습니다.", SellerProfileResponse.from(seller)));
    }
}
