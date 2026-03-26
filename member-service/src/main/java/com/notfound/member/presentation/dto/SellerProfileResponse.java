package com.notfound.member.presentation.dto;

import com.notfound.member.domain.model.Seller;

import java.util.UUID;

public record SellerProfileResponse(
        UUID sellerId,
        String businessNumber,
        String shopName,
        String bankCode,
        String bankAccount,
        String accountHolder,
        String status
) {

    public static SellerProfileResponse from(Seller seller) {
        return new SellerProfileResponse(
                seller.getId(),
                seller.getBusinessNumber(),
                seller.getShopName(),
                seller.getBankCode(),
                seller.getBankAccount(),
                seller.getAccountHolder(),
                seller.getStatus().name()
        );
    }
}
