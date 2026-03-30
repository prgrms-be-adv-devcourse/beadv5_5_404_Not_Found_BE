package com.notfound.member.presentation.controller;

import com.notfound.member.application.port.in.CreateAddressUseCase;
import com.notfound.member.application.port.in.DeleteAddressUseCase;
import com.notfound.member.application.port.in.GetMemberAddressesUseCase;
import com.notfound.member.application.port.in.UpdateAddressUseCase;
import com.notfound.member.domain.model.Address;
import com.notfound.member.infrastructure.security.AuthUser;
import com.notfound.member.infrastructure.security.AuthenticatedUser;
import com.notfound.member.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Address", description = "배송지 API")
@RestController
@RequestMapping("/member/address")
public class MemberAddressController {

    private final GetMemberAddressesUseCase getMemberAddressesUseCase;
    private final CreateAddressUseCase createAddressUseCase;
    private final UpdateAddressUseCase updateAddressUseCase;
    private final DeleteAddressUseCase deleteAddressUseCase;

    public MemberAddressController(GetMemberAddressesUseCase getMemberAddressesUseCase,
                                   CreateAddressUseCase createAddressUseCase,
                                   UpdateAddressUseCase updateAddressUseCase,
                                   DeleteAddressUseCase deleteAddressUseCase) {
        this.getMemberAddressesUseCase = getMemberAddressesUseCase;
        this.createAddressUseCase = createAddressUseCase;
        this.updateAddressUseCase = updateAddressUseCase;
        this.deleteAddressUseCase = deleteAddressUseCase;
    }

    @Operation(summary = "배송지 목록 조회", description = "배송지 리스트")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            @AuthUser AuthenticatedUser user) {

        UUID memberId = UUID.fromString(user.userId());
        List<AddressResponse> addresses = getMemberAddressesUseCase.getAddresses(memberId)
                .stream()
                .map(AddressResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(200, "ADDRESS_LIST_FETCH_SUCCESS",
                        "배송지 목록 조회에 성공했습니다.", addresses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody CreateAddressRequest request) {

        UUID memberId = UUID.fromString(user.userId());
        Address address = createAddressUseCase.createAddress(memberId, request.toCommand());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "ADDRESS_CREATE_SUCCESS",
                        "배송지가 등록되었습니다.", AddressResponse.from(address)));
    }

    @Operation(summary = "배송지 수정", description = "배송지 정보 수정")
    @PatchMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthUser AuthenticatedUser user,
            @PathVariable UUID addressId,
            @RequestBody UpdateAddressRequest request) {

        UUID memberId = UUID.fromString(user.userId());
        Address updated = updateAddressUseCase.updateAddress(
                memberId, addressId, request.toCommand());

        return ResponseEntity.ok(
                ApiResponse.success(200, "ADDRESS_UPDATE_SUCCESS",
                        "배송지 정보가 수정되었습니다.", AddressResponse.from(updated)));
    }

    @Operation(summary = "배송지 삭제", description = "배송지 삭제")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthUser AuthenticatedUser user,
            @PathVariable UUID addressId) {

        UUID memberId = UUID.fromString(user.userId());
        deleteAddressUseCase.deleteAddress(memberId, addressId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "ADDRESS_DELETE_SUCCESS",
                        "배송지가 삭제되었습니다.", null));
    }
}
