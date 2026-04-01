package com.notfound.member.presentation.dto;

import com.notfound.member.domain.model.Address;

import java.util.UUID;

public record AddressResponse(
        UUID addressId,
        String recipient,
        String phone,
        String zipcode,
        String address1,
        String address2,
        boolean isDefault
) {

    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getRecipient(),
                address.getPhone(),
                address.getZipcode(),
                address.getAddress1(),
                address.getAddress2(),
                address.isDefault()
        );
    }
}
