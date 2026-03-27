package com.notfound.member.presentation.dto;

import com.notfound.member.domain.model.Seller;

import java.util.UUID;

public record RegisterSellerResponse(
        UUID sellerId,
        UUID memberId,
        String shopName,
        String status
) {

    public static RegisterSellerResponse from(Seller seller) {
        return new RegisterSellerResponse(
                seller.getId(),
                seller.getMemberId(),
                seller.getShopName(),
                seller.getStatus().name()
        );
    }
}
