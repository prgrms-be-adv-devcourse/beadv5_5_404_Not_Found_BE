package com.notfound.member.presentation.dto;

import com.notfound.member.application.port.in.command.UpdateAddressCommand;
import jakarta.validation.constraints.Size;

public record UpdateAddressRequest(
        @Size(max = 100, message = "수령인 이름은 100자 이하여야 합니다.")
        String recipient,

        @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
        String phone,

        @Size(max = 10, message = "우편번호는 10자 이하여야 합니다.")
        String zipcode,

        @Size(max = 255, message = "기본 주소는 255자 이하여야 합니다.")
        String address1,

        @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다.")
        String address2,

        Boolean isDefault
) {

    public UpdateAddressCommand toCommand() {
        return new UpdateAddressCommand(recipient, phone, zipcode, address1, address2, isDefault);
    }
}
