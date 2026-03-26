package com.notfound.member.application.port.out;

import com.notfound.member.domain.model.Address;

import java.util.List;
import java.util.UUID;

public interface AddressRepository {

    List<Address> findByMemberIdAndIsDeletedFalse(UUID memberId);
}
