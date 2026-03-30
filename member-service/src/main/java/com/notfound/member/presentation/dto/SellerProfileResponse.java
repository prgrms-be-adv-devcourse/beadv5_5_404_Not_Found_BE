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
                mask(seller.getBusinessNumber(), 3, 3),
                seller.getShopName(),
                seller.getBankCode(),
                mask(seller.getBankAccount(), 4, 4),
                mask(seller.getAccountHolder(), 1, 0),
                seller.getStatus().name()
        );
    }

    private static String mask(String value, int prefixLen, int suffixLen) {
        if (value == null) return null;
        if (value.length() <= prefixLen + suffixLen) return "*".repeat(value.length());
        String prefix = value.substring(0, prefixLen);
        String suffix = suffixLen > 0 ? value.substring(value.length() - suffixLen) : "";
        String masked = "*".repeat(value.length() - prefixLen - suffixLen);
        return prefix + masked + suffix;
    }
}
