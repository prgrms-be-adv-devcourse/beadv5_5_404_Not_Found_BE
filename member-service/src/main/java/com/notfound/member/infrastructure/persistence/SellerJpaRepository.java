package com.notfound.member.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SellerJpaRepository extends JpaRepository<SellerJpaEntity, UUID> {

    Optional<SellerJpaEntity> findByMemberId(UUID memberId);

    boolean existsByMemberId(UUID memberId);
}
