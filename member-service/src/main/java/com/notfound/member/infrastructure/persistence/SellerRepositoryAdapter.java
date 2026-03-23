package com.notfound.member.infrastructure.persistence;

import com.notfound.member.application.port.out.SellerRepository;
import com.notfound.member.domain.model.Seller;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SellerRepositoryAdapter implements SellerRepository {

    private final SellerJpaRepository sellerJpaRepository;

    public SellerRepositoryAdapter(SellerJpaRepository sellerJpaRepository) {
        this.sellerJpaRepository = sellerJpaRepository;
    }

    @Override
    public Optional<Seller> findByMemberId(UUID memberId) {
        return sellerJpaRepository.findByMemberId(memberId).map(SellerJpaEntity::toDomain);
    }
}
