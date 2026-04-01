package com.notfound.settlement.domain.exception;

import java.util.UUID;

public class SellerAccountNotFoundException extends RuntimeException {

    public SellerAccountNotFoundException(UUID sellerId) {
        super("판매자 계좌 정보를 찾을 수 없습니다. sellerId=" + sellerId);
    }
}
