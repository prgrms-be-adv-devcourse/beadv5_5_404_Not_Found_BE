package com.notfound.product.application.port.out;

import java.util.UUID;

public interface SellerStatusVerifier {

    boolean isApprovedSeller(UUID memberId);
}
