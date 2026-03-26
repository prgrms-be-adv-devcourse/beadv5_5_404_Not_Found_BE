package com.notfound.member.infrastructure.persistence;

import com.notfound.member.application.port.out.AddressRepository;
import com.notfound.member.domain.model.Address;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AddressRepositoryAdapter implements AddressRepository {

    private final AddressJpaRepository addressJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    public AddressRepositoryAdapter(AddressJpaRepository addressJpaRepository,
                                    MemberJpaRepository memberJpaRepository) {
        this.addressJpaRepository = addressJpaRepository;
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public List<Address> findByMemberIdAndIsDeletedFalse(UUID memberId) {
        return addressJpaRepository.findByMemberIdAndIsDeletedFalse(memberId)
                .stream()
                .map(AddressJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Address> findById(UUID addressId) {
        return addressJpaRepository.findById(addressId).map(AddressJpaEntity::toDomain);
    }

    @Override
    public Address save(Address address) {
        MemberJpaEntity memberEntity = memberJpaRepository.getReferenceById(address.getMemberId());
        AddressJpaEntity entity = AddressJpaEntity.from(address, memberEntity);
        AddressJpaEntity saved = addressJpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public void softDelete(UUID addressId) {
        addressJpaRepository.softDeleteById(addressId);
    }
}
