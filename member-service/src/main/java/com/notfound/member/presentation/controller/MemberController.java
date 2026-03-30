package com.notfound.member.presentation.controller;

import com.notfound.member.application.port.in.CheckSellerRegisteredUseCase;
import com.notfound.member.application.port.in.GetDepositBalanceUseCase;
import com.notfound.member.application.port.in.GetMemberProfileUseCase;
import com.notfound.member.application.port.in.UpdateMemberUseCase;
import com.notfound.member.application.port.in.WithdrawMemberUseCase;
import com.notfound.member.application.port.in.command.UpdateMemberCommand;
import com.notfound.member.domain.model.Member;
import com.notfound.member.infrastructure.security.AuthUser;
import com.notfound.member.infrastructure.security.AuthenticatedUser;
import com.notfound.member.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/member/me")
public class MemberController {

    private final GetMemberProfileUseCase getMemberProfileUseCase;
    private final UpdateMemberUseCase updateMemberUseCase;
    private final GetDepositBalanceUseCase getDepositBalanceUseCase;
    private final WithdrawMemberUseCase withdrawMemberUseCase;
    private final CheckSellerRegisteredUseCase checkSellerRegisteredUseCase;

    public MemberController(GetMemberProfileUseCase getMemberProfileUseCase,
                             UpdateMemberUseCase updateMemberUseCase,
                             GetDepositBalanceUseCase getDepositBalanceUseCase,
                             WithdrawMemberUseCase withdrawMemberUseCase,
                             CheckSellerRegisteredUseCase checkSellerRegisteredUseCase) {
        this.getMemberProfileUseCase = getMemberProfileUseCase;
        this.updateMemberUseCase = updateMemberUseCase;
        this.getDepositBalanceUseCase = getDepositBalanceUseCase;
        this.withdrawMemberUseCase = withdrawMemberUseCase;
        this.checkSellerRegisteredUseCase = checkSellerRegisteredUseCase;
    }

    @Operation(summary = "내 정보 조회", description = "회원 정보 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMyProfile(
            @AuthUser AuthenticatedUser user) {

        UUID memberId = UUID.fromString(user.userId());
        Member member = getMemberProfileUseCase.getProfile(memberId);
        boolean sellerRegistered = checkSellerRegisteredUseCase.isSellerRegistered(memberId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "MEMBER_INFO_FETCH_SUCCESS",
                        "내 정보 조회에 성공했습니다.",
                        MemberProfileResponse.from(member, sellerRegistered)));
    }

    @Operation(summary = "내 정보 수정", description = "이름, 전화번호 수정")
    @PatchMapping
    public ResponseEntity<ApiResponse<UpdateMemberResponse>> updateMyProfile(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody UpdateMemberRequest request) {

        UUID memberId = UUID.fromString(user.userId());
        Member member = updateMemberUseCase.updateMember(memberId, request.toCommand());

        return ResponseEntity.ok(
                ApiResponse.success(200, "MEMBER_UPDATE_SUCCESS",
                        "회원 정보가 수정되었습니다.",
                        UpdateMemberResponse.from(member)));
    }

    @Operation(summary = "예치금 잔액 조회", description = "보유 예치금 조회")
    @GetMapping("/deposit")
    public ResponseEntity<ApiResponse<DepositBalanceResponse>> getDepositBalance(
            @AuthUser AuthenticatedUser user) {

        UUID memberId = UUID.fromString(user.userId());
        int balance = getDepositBalanceUseCase.getDepositBalance(memberId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "DEPOSIT_BALANCE_FOUND",
                        "예치금 잔액을 조회했습니다.",
                        new DepositBalanceResponse(memberId, balance)));
    }

    @Operation(summary = "회원 탈퇴", description = "비밀번호 확인 후 탈퇴")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthUser AuthenticatedUser user,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody WithdrawMemberRequest request) {

        UUID memberId = UUID.fromString(user.userId());
        String accessToken = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;
        withdrawMemberUseCase.withdraw(memberId, request.password(), accessToken);

        return ResponseEntity.ok(
                ApiResponse.success(200, "MEMBER_DELETE_SUCCESS",
                        "회원 탈퇴가 완료되었습니다.", null));
    }
}
