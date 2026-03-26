package com.notfound.member.application.port.in;

import com.notfound.member.application.port.in.command.RegisterSellerCommand;
import com.notfound.member.domain.model.Seller;

import java.util.UUID;

public interface RegisterSellerUseCase {

    Seller registerSeller(UUID memberId, RegisterSellerCommand command);
}
