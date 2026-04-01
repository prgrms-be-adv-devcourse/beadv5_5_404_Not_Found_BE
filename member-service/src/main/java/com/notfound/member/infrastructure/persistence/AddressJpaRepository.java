package com.notfound.member.infrastructure.persistence;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressJpaRepository extends JpaRepository<AddressJpaEntity, UUID> {

    List<AddressJpaEntity> findByMemberIdAndIsDeletedFalse(UUID memberId);

    @Modifying
    @Query("UPDATE AddressJpaEntity a SET a.isDeleted = true WHERE a.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
