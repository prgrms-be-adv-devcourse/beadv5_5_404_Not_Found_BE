package com.notfound.member.presentation.dto;

import com.notfound.member.application.port.in.command.CreateAddressCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAddressRequest(

        @Size(max = 50, message = "라벨은 50자 이하여야 합니다.")
        String label,

        @NotBlank(message = "수령인은 필수입니다.")
        @Size(max = 100, message = "수령인은 100자 이하여야 합니다.")
        String recipient,

        @NotBlank(message = "연락처는 필수입니다.")
        @Size(max = 20, message = "연락처는 20자 이하여야 합니다.")
        String phone,

        @NotBlank(message = "우편번호는 필수입니다.")
        @Size(max = 10, message = "우편번호는 10자 이하여야 합니다.")
        String zipcode,

        @NotBlank(message = "주소는 필수입니다.")
        String address1,

        String address2
) {

    public CreateAddressCommand toCommand() {
        return new CreateAddressCommand(label, recipient, phone, zipcode, address1, address2);
    }
}
