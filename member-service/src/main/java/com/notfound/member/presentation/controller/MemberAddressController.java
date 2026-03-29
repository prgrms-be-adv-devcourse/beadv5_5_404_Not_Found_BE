package com.notfound.member.presentation.controller;

import com.notfound.member.application.port.in.CreateAddressUseCase;
import com.notfound.member.application.port.in.DeleteAddressUseCase;
import com.notfound.member.domain.model.Address;
import com.notfound.member.infrastructure.security.AuthUser;
import com.notfound.member.infrastructure.security.AuthenticatedUser;
import com.notfound.member.presentation.dto.AddressResponse;
import com.notfound.member.presentation.dto.ApiResponse;
import com.notfound.member.presentation.dto.CreateAddressRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/member/address")
public class MemberAddressController {

    private final CreateAddressUseCase createAddressUseCase;
    private final DeleteAddressUseCase deleteAddressUseCase;

    public MemberAddressController(CreateAddressUseCase createAddressUseCase,
                                   DeleteAddressUseCase deleteAddressUseCase) {
        this.createAddressUseCase = createAddressUseCase;
        this.deleteAddressUseCase = deleteAddressUseCase;
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
