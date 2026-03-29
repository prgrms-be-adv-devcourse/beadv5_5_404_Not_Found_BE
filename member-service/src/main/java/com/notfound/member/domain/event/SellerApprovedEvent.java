package com.notfound.member.domain.event;

import java.util.UUID;

public record SellerApprovedEvent(
        UUID memberId,
        UUID sellerId,
        String shopName
) {
}
