package com.notfound.member.infrastructure.persistence;

import com.notfound.member.application.port.out.AddressRepository;
import com.notfound.member.domain.model.Address;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AddressRepositoryAdapter implements AddressRepository {

    private final AddressJpaRepository addressJpaRepository;

    public AddressRepositoryAdapter(AddressJpaRepository addressJpaRepository) {
        this.addressJpaRepository = addressJpaRepository;
    }

    @Override
    public List<Address> findByMemberIdAndIsDeletedFalse(UUID memberId) {
        return addressJpaRepository.findByMemberIdAndIsDeletedFalse(memberId)
                .stream()
                .map(AddressJpaEntity::toDomain)
                .toList();
    }
}
