package com.notfound.member.application.port.in;

import java.util.UUID;

public interface DeleteAddressUseCase {

    void deleteAddress(UUID memberId, UUID addressId);
}
