package com.notfound.member.presentation.dto;

import com.notfound.member.application.port.in.command.RegisterSellerCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterSellerRequest(

        @NotBlank(message = "사업자등록번호는 필수입니다.")
        @Size(max = 20, message = "사업자등록번호는 20자 이하여야 합니다.")
        String businessNumber,

        @NotBlank(message = "상호명은 필수입니다.")
        @Size(max = 100, message = "상호명은 100자 이하여야 합니다.")
        String shopName,

        @NotBlank(message = "은행 코드는 필수입니다.")
        @Size(max = 10, message = "은행 코드는 10자 이하여야 합니다.")
        String bankCode,

        @NotBlank(message = "계좌번호는 필수입니다.")
        @Size(max = 50, message = "계좌번호는 50자 이하여야 합니다.")
        String bankAccount,

        @NotBlank(message = "예금주명은 필수입니다.")
        @Size(max = 100, message = "예금주명은 100자 이하여야 합니다.")
        String accountHolder
) {

    public RegisterSellerCommand toCommand() {
        return new RegisterSellerCommand(businessNumber, shopName, bankCode, bankAccount, accountHolder);
    }
}
