package com.notfound.member.application.port.in.command;

public record UpdateAddressCommand(
        String recipient,
        String phone,
        String zipcode,
        String address1,
        String address2,
        Boolean isDefault
) {
}
