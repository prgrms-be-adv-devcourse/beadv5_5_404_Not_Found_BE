package com.notfound.member.application.port.in.command;

public record RegisterSellerCommand(
        String businessNumber,
        String shopName,
        String bankCode,
        String bankAccount,
        String accountHolder
) {
}
