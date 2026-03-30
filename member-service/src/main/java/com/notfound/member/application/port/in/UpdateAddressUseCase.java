package com.notfound.member.application.port.in;

import com.notfound.member.application.port.in.command.UpdateAddressCommand;
import com.notfound.member.domain.model.Address;

import java.util.UUID;

public interface UpdateAddressUseCase {
    Address updateAddress(UUID memberId, UUID addressId, UpdateAddressCommand command);
}
