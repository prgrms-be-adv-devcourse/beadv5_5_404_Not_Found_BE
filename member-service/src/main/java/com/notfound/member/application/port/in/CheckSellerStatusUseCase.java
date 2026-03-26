package com.notfound.member.application.port.in;

import java.util.UUID;

public interface CheckSellerStatusUseCase {

    boolean isApprovedSeller(UUID memberId);
}
