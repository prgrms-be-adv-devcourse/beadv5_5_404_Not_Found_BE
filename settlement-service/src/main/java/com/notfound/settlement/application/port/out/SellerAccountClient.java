package com.notfound.settlement.application.port.out;

import java.util.UUID;

public interface SellerAccountClient {

    SellerAccount findSellerAccount(UUID sellerId);
}
