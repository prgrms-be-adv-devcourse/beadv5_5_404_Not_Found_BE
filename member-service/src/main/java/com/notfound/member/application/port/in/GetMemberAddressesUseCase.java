package com.notfound.member.application.port.in;

import com.notfound.member.domain.model.Address;

import java.util.List;
import java.util.UUID;

public interface GetMemberAddressesUseCase {

    List<Address> getAddresses(UUID memberId);
}
