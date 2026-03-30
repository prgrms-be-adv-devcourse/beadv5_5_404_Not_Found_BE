package com.notfound.member.application.port.in;

import com.notfound.member.domain.model.Seller;
import com.notfound.member.domain.model.SellerStatus;

import java.util.UUID;

public interface ApproveSellerUseCase {

    Seller updateSellerStatus(UUID memberId, SellerStatus status);
}
