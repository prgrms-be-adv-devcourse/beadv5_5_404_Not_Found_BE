package com.notfound.member.presentation.controller;

import com.notfound.member.application.port.in.ChargeDepositUseCase;
import com.notfound.member.application.port.in.CheckMemberActiveUseCase;
import com.notfound.member.application.port.in.DeductDepositUseCase;
import com.notfound.member.application.port.in.GetDepositBalanceUseCase;
import com.notfound.member.application.port.in.GetMemberAddressesUseCase;
import com.notfound.member.presentation.dto.AddressResponse;
import com.notfound.member.presentation.dto.ApiResponse;
import com.notfound.member.presentation.dto.DepositRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Internal - Member", description = "내부 회원 API (서비스 간 통신)")
@RestController
@RequestMapping("/internal/member")
public class InternalMemberController {

    private final GetMemberAddressesUseCase getMemberAddressesUseCase;
    private final CheckMemberActiveUseCase checkMemberActiveUseCase;
    private final GetDepositBalanceUseCase getDepositBalanceUseCase;
    private final DeductDepositUseCase deductDepositUseCase;
    private final ChargeDepositUseCase chargeDepositUseCase;

    public InternalMemberController(GetMemberAddressesUseCase getMemberAddressesUseCase,
                                    CheckMemberActiveUseCase checkMemberActiveUseCase,
                                    GetDepositBalanceUseCase getDepositBalanceUseCase,
                                    DeductDepositUseCase deductDepositUseCase,
                                    ChargeDepositUseCase chargeDepositUseCase) {
        this.getMemberAddressesUseCase = getMemberAddressesUseCase;
        this.checkMemberActiveUseCase = checkMemberActiveUseCase;
        this.getDepositBalanceUseCase = getDepositBalanceUseCase;
        this.deductDepositUseCase = deductDepositUseCase;
        this.chargeDepositUseCase = chargeDepositUseCase;
    }

    @Operation(summary = "배송지 목록 조회", description = "회원의 배송지 목록 조회 (주문 서비스용)")
    @GetMapping("/{memberId}/address")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            @PathVariable UUID memberId) {

        List<AddressResponse> addresses = getMemberAddressesUseCase.getAddresses(memberId)
                .stream()
                .map(AddressResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(200, "ADDRESS_LIST_FETCH_SUCCESS",
                        "배송지 목록 조회에 성공했습니다.", addresses));
    }

    @Operation(summary = "회원 활성 상태 확인", description = "회원 ACTIVE 여부 확인 (주문/결제 서비스용)")
    @GetMapping("/{memberId}/active")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> isActiveMember(
            @PathVariable UUID memberId) {

        boolean active = checkMemberActiveUseCase.isActiveMember(memberId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "MEMBER_ACTIVE_CHECK_SUCCESS",
                        "회원 활성 상태 조회가 완료되었습니다.",
                        Map.of("active", active)));
    }

    @Operation(summary = "예치금 잔액 조회", description = "회원 예치금 잔액 확인 (결제 서비스용)")
    @GetMapping("/{memberId}/deposit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDepositBalance(
            @PathVariable UUID memberId) {

        int balance = getDepositBalanceUseCase.getDepositBalance(memberId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "DEPOSIT_BALANCE_FOUND",
                        "예치금 잔액을 조회했습니다.",
                        Map.of("memberId", memberId, "depositBalance", balance)));
    }

    @Operation(summary = "예치금 차감", description = "결제 시 예치금 차감 (결제 서비스용)")
    @PostMapping("/{memberId}/deposit/deduct")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deductDeposit(
            @PathVariable UUID memberId,
            @Valid @RequestBody DepositRequest request) {

        int remaining = deductDepositUseCase.deductDeposit(memberId, request.amount(), request.transactionId());

        return ResponseEntity.ok(
                ApiResponse.success(200, "DEPOSIT_DEDUCT_SUCCESS",
                        "예치금이 차감되었습니다.",
                        Map.of("memberId", memberId, "remainingBalance", remaining)));
    }

    @Operation(summary = "예치금 충전", description = "충전 또는 환불 시 예치금 증가 (결제 서비스용)")
    @PostMapping("/{memberId}/deposit/charge")
    public ResponseEntity<ApiResponse<Map<String, Object>>> chargeDeposit(
            @PathVariable UUID memberId,
            @Valid @RequestBody DepositRequest request) {

        int remaining = chargeDepositUseCase.chargeDeposit(memberId, request.amount(), request.transactionId());

        return ResponseEntity.ok(
                ApiResponse.success(200, "DEPOSIT_CHARGE_SUCCESS",
                        "예치금이 충전되었습니다.",
                        Map.of("memberId", memberId, "remainingBalance", remaining)));
    }
}
