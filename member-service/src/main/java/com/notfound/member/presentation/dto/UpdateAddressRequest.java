package com.notfound.member.presentation.dto;

import com.notfound.member.application.port.in.command.UpdateAddressCommand;

public record UpdateAddressRequest(
        String recipient,
        String phone,
        String zipcode,
        String address1,
        String address2,
        Boolean isDefault
) {
    public UpdateAddressCommand toCommand() {
        return new UpdateAddressCommand(recipient, phone, zipcode, address1, address2, isDefault);
    }
}
