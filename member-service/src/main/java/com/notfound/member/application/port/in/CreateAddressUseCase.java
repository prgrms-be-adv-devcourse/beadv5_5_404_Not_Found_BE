package com.notfound.member.application.port.in;

import com.notfound.member.application.port.in.command.CreateAddressCommand;
import com.notfound.member.domain.model.Address;

import java.util.UUID;

public interface CreateAddressUseCase {

    Address createAddress(UUID memberId, CreateAddressCommand command);
}
