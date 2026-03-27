package com.notfound.member.presentation.dto;

import com.notfound.member.domain.model.Seller;

import java.util.UUID;

public record SellerAccountResponse(
        UUID memberId,
        String bankCode,
        String bankAccount,
        String accountHolder
) {

    public static SellerAccountResponse from(Seller seller) {
        return new SellerAccountResponse(
                seller.getMemberId(),
                seller.getBankCode(),
                seller.getBankAccount(),
                seller.getAccountHolder()
        );
    }
}
