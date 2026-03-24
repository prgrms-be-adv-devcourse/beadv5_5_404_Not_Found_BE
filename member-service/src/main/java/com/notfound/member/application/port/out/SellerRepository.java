package com.notfound.member.application.port.out;

import com.notfound.member.domain.model.Seller;

import java.util.Optional;
import java.util.UUID;

public interface SellerRepository {

    Optional<Seller> findByMemberId(UUID memberId);
}
