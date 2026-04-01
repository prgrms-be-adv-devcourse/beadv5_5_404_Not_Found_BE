package com.notfound.member.application.service;

import com.notfound.member.application.port.in.CreateAddressUseCase;
import com.notfound.member.application.port.in.DeleteAddressUseCase;
import com.notfound.member.application.port.in.GetMemberAddressesUseCase;
import com.notfound.member.application.port.in.UpdateAddressUseCase;
import com.notfound.member.application.port.in.command.CreateAddressCommand;
import com.notfound.member.application.port.in.command.UpdateAddressCommand;
import com.notfound.member.application.port.out.AddressRepository;
import com.notfound.member.domain.exception.MemberException;
import com.notfound.member.domain.model.Address;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AddressService implements GetMemberAddressesUseCase, CreateAddressUseCase,
        UpdateAddressUseCase, DeleteAddressUseCase {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> getAddresses(UUID memberId) {
        return addressRepository.findByMemberIdAndIsDeletedFalse(memberId);
    }

    @Override
    @Transactional
    public Address createAddress(UUID memberId, CreateAddressCommand command) {
        long currentCount = addressRepository.findByMemberIdAndIsDeletedFalse(memberId).size();
        if (currentCount >= 10) {
            throw MemberException.addressLimitExceeded();
        }

        Address address = Address.builder()
                .memberId(memberId)
                .label(command.label())
                .recipient(command.recipient())
                .phone(command.phone())
                .zipcode(command.zipcode())
                .address1(command.address1())
                .address2(command.address2())
                .isDefault(false)
                .isDeleted(false)
                .build();

        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public Address updateAddress(UUID memberId, UUID addressId, UpdateAddressCommand command) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(MemberException::addressNotFound);

        if (!address.getMemberId().equals(memberId)) {
            throw MemberException.accessDenied();
        }

        // 기본 배송지로 설정 시 기존 기본 배송지 해제
        if (Boolean.TRUE.equals(command.isDefault())) {
            addressRepository.findByMemberIdAndIsDeletedFalse(memberId).stream()
                    .filter(Address::isDefault)
                    .filter(a -> !a.getId().equals(addressId))
                    .forEach(a -> {
                        a.update(null, null, null, null, null, false);
                        addressRepository.save(a);
                    });
        }

        address.update(command.recipient(), command.phone(), command.zipcode(),
                command.address1(), command.address2(), command.isDefault());

        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public void deleteAddress(UUID memberId, UUID addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(MemberException::addressNotFound);

        if (!address.getMemberId().equals(memberId)) {
            throw MemberException.accessDenied();
        }

        addressRepository.softDelete(addressId);
    }
}
