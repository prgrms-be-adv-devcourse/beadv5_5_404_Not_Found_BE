package com.notfound.member.application.port.in;

import java.util.UUID;

public interface CheckSellerRegisteredUseCase {

    boolean isSellerRegistered(UUID memberId);
}
