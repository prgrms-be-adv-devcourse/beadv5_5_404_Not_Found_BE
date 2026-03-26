package com.notfound.member.application.service;

import com.notfound.member.application.port.in.GetMemberAddressesUseCase;
import com.notfound.member.application.port.out.AddressRepository;
import com.notfound.member.domain.model.Address;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AddressService implements GetMemberAddressesUseCase {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> getAddresses(UUID memberId) {
        return addressRepository.findByMemberIdAndIsDeletedFalse(memberId);
    }
}
