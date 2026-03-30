package com.notfound.settlement.application.port.out;

public record SellerAccount(
        String bankCode,
        String bankAccount,
        String accountHolder
) {}
