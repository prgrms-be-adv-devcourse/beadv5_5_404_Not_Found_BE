package com.notfound.member.application.port.in.command;

public record CreateAddressCommand(
        String label,
        String recipient,
        String phone,
        String zipcode,
        String address1,
        String address2
) {
}
