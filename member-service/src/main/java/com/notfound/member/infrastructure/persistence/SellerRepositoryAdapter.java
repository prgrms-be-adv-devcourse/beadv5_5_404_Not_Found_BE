package com.notfound.member.infrastructure.persistence;

import com.notfound.member.application.port.out.SellerRepository;
import com.notfound.member.domain.model.Seller;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SellerRepositoryAdapter implements SellerRepository {

    private final SellerJpaRepository sellerJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    public SellerRepositoryAdapter(SellerJpaRepository sellerJpaRepository,
                                   MemberJpaRepository memberJpaRepository) {
        this.sellerJpaRepository = sellerJpaRepository;
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public Optional<Seller> findById(UUID sellerId) {
        return sellerJpaRepository.findById(sellerId).map(SellerJpaEntity::toDomain);
    }

    @Override
    public Optional<Seller> findByMemberId(UUID memberId) {
        return sellerJpaRepository.findByMemberId(memberId).map(SellerJpaEntity::toDomain);
    }

    @Override
    public boolean existsByMemberId(UUID memberId) {
        return sellerJpaRepository.existsByMemberId(memberId);
    }

    @Override
    public Seller save(Seller seller) {
        MemberJpaEntity memberEntity = memberJpaRepository.getReferenceById(seller.getMemberId());
        SellerJpaEntity entity = SellerJpaEntity.from(seller, memberEntity);
        SellerJpaEntity saved = sellerJpaRepository.save(entity);
        return saved.toDomain();
    }
}
