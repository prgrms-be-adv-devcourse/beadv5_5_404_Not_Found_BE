package com.notfound.member.application.port.in;

import com.notfound.member.domain.model.Seller;

import java.util.UUID;

public interface GetSellerAccountUseCase {

    Seller getSellerAccount(UUID memberId);
}
